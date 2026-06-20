package com.unidocs.controller;

import com.unidocs.domain.Document;
import com.unidocs.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DocumentService documentService;
    private final com.unidocs.service.ReportService reportService;
    private final com.unidocs.service.DeduplicationService deduplicationService;
    private final com.unidocs.service.BulkImportService bulkImportService;

    public AdminController(DocumentService documentService, 
                           com.unidocs.service.ReportService reportService,
                           com.unidocs.service.DeduplicationService deduplicationService,
                           com.unidocs.service.BulkImportService bulkImportService) {
        this.documentService = documentService;
        this.reportService = reportService;
        this.deduplicationService = deduplicationService;
        this.bulkImportService = bulkImportService;
    }

    @GetMapping("/documents")
    public String viewDocuments(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {
        
        Page<Document> documentPage = documentService.getAllDocumentsPaginated(page, size, status);
        
        model.addAttribute("documentPage", documentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", documentPage.getTotalPages());
        
        return "admin/documents";
    }

    @PostMapping("/documents/{id}/approve")
    public String approveDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.approveDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Duyệt tài liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @PostMapping("/documents/bulk-approve")
    public String bulkApproveDocuments(@RequestParam(value="ids", required=false) java.util.List<Long> ids, RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 tài liệu để duyệt.");
            return "redirect:/admin/documents";
        }
        try {
            int count = 0;
            for (Long id : ids) {
                documentService.approveDocument(id);
                count++;
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt hàng loạt " + count + " tài liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi duyệt hàng loạt: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @PostMapping("/documents/{id}/reject")
    public String rejectDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.rejectDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa/từ chối tài liệu!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @GetMapping("/reports")
    public String viewReports(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {
        
        Page<com.unidocs.domain.DocumentReport> reportPage = reportService.getAllReportsPaginated(page, size, status);
        
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        
        return "admin/reports";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportService.resolveReport(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đánh dấu báo cáo là đã xử lý.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{id}/delete-document")
    public String deleteReportedDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportService.deleteReportedDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài liệu vi phạm thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/system/deduplicate")
    public String deduplicateSystem(RedirectAttributes redirectAttributes) {
        try {
            deduplicationService.deduplicateFaculties();
            deduplicationService.deduplicateCourses();
            redirectAttributes.addFlashAttribute("successMessage", "Dọn dẹp và gộp dữ liệu trùng lặp thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi dọn dẹp: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }

    @PostMapping("/system/import-zip")
    public String importZip(@RequestParam("zipFile") org.springframework.web.multipart.MultipartFile zipFile, RedirectAttributes redirectAttributes) {
        try {
            bulkImportService.importFromZip(zipFile);
            redirectAttributes.addFlashAttribute("successMessage", "Nhập liệu hàng loạt từ file ZIP thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi nhập liệu: " + e.getMessage());
        }
        return "redirect:/admin/documents";
    }
}