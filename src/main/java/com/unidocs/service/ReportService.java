package com.unidocs.service;

import com.unidocs.domain.Document;
import com.unidocs.domain.DocumentReport;
import com.unidocs.domain.DocumentReportStatus;
import com.unidocs.domain.DocumentStatus;
import com.unidocs.dto.ReportDto;
import com.unidocs.dto.ReportNotificationEvent;
import com.unidocs.repository.DocumentRepository;
import com.unidocs.repository.DocumentReportRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Service
public class ReportService {

    private final DocumentReportRepository reportRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentService documentService;

    public ReportService(DocumentReportRepository reportRepository, DocumentRepository documentRepository, ApplicationEventPublisher eventPublisher, DocumentService documentService) {
        this.reportRepository = reportRepository;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
        this.documentService = documentService;
    }

    @Transactional
    public void submitReport(ReportDto reportDto) {
        Optional<Document> docOpt = documentRepository.findById(reportDto.getDocumentId());
        if (docOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found");
        }
        
        Document document = docOpt.get();
        
        DocumentReport report = new DocumentReport();
        report.setDocument(document);
        report.setReportType(reportDto.getReportType());
        report.setMessage(reportDto.getMessage());
        report.setStatus(DocumentReportStatus.PENDING);
        
        DocumentReport savedReport = reportRepository.save(report);
        
        // Trigger automated scan asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            automatedScanAndNotify(savedReport, document);
        });
    }

    protected void automatedScanAndNotify(DocumentReport report, Document document) {
        long pendingCount = reportRepository.countByDocumentIdAndStatus(document.getId(), DocumentReportStatus.PENDING);
        
        String priority = "LOW";
        boolean fileIsBroken = false;
        
        // Automated File Integrity Check (Scan)
        try {
            URL url = new URL(document.getStorageUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 404 || responseCode >= 500) {
                fileIsBroken = true;
            }
        } catch (Exception e) {
            fileIsBroken = true; // Connection failed, treat as broken
        }

        if (fileIsBroken) {
            priority = "HIGH";
            // Optionally, we could automatically hide the document here:
            // document.setStatus(DocumentStatus.ERROR);
            // documentRepository.save(document);
        } else if (pendingCount >= 3) {
            priority = "HIGH";
        } else if (pendingCount == 2) {
            priority = "MEDIUM";
        }

        // Emit real-time notification event
        ReportNotificationEvent event = new ReportNotificationEvent(
                report.getId(),
                document.getId(),
                document.getTitle(),
                report.getReportType(),
                priority,
                pendingCount
        );
        eventPublisher.publishEvent(event);
    }

    public org.springframework.data.domain.Page<DocumentReport> getAllReportsPaginated(int page, int size, String status) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (status != null && !status.isEmpty()) {
            try {
                DocumentReportStatus docStatus = DocumentReportStatus.valueOf(status.toUpperCase());
                return reportRepository.findByStatusOrderByCreatedAtDesc(docStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }
        return reportRepository.findAllByOrderByStatusAscCreatedAtDesc(pageable);
    }

    @Transactional
    public void resolveReport(Long reportId) {
        DocumentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo"));
        report.setStatus(DocumentReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    @Transactional
    public void deleteReportedDocument(Long reportId) {
        DocumentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo"));
        
        Document doc = report.getDocument();
        
        // Before deleting the document, we must delete all related reports to avoid FK constraint violations
        // Or we could update reportRepository to delete by documentId
        // But since we just want a simple solution, we'll mark the document as ERROR/HIDDEN or actually delete
        // If we really delete it, we must delete all reports first.
        // Let's delete all reports for this document first.
        reportRepository.deleteAll(reportRepository.findByStatusOrderByCreatedAtDesc(DocumentReportStatus.PENDING).stream().filter(r -> r.getDocument().getId().equals(doc.getId())).toList());
        reportRepository.deleteAll(reportRepository.findByStatusOrderByCreatedAtDesc(DocumentReportStatus.RESOLVED).stream().filter(r -> r.getDocument().getId().equals(doc.getId())).toList());

        // Now delete the document properly using DocumentService
        documentService.rejectDocument(doc.getId());
    }
}
