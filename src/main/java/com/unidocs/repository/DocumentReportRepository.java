package com.unidocs.repository;

import com.unidocs.domain.DocumentReport;
import com.unidocs.domain.DocumentReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DocumentReportRepository extends JpaRepository<DocumentReport, Long> {
    long countByDocumentIdAndStatus(Long documentId, DocumentReportStatus status);
    List<DocumentReport> findByStatusOrderByCreatedAtDesc(DocumentReportStatus status);
    Page<DocumentReport> findByStatusOrderByCreatedAtDesc(DocumentReportStatus status, Pageable pageable);
    Page<DocumentReport> findAllByOrderByStatusAscCreatedAtDesc(Pageable pageable);
}
