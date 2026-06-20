package com.unidocs.controller;

import com.unidocs.domain.Document;
import com.unidocs.domain.DocumentStatus;
import com.unidocs.service.DocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/document")
public class DocumentWebController {

    private final DocumentService documentService;

    public DocumentWebController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{slug}/view")
    public String viewDocument(@PathVariable String slug, Model model) {
        Document doc = documentService.getDocumentBySlug(slug);
        
        // Only allow viewing if approved
        if (doc.getStatus() != DocumentStatus.APPROVED) {
            return "redirect:/"; // Or a proper error page
        }

        documentService.incrementViews(doc.getId());
        
        // Redirect directly to the Supabase S3 URL to view
        return "redirect:" + doc.getStorageUrl();
    }

    @GetMapping("/{slug}/download")
    public String downloadDocument(@PathVariable String slug) {
        Document doc = documentService.getDocumentBySlug(slug);
        
        if (doc.getStatus() != DocumentStatus.APPROVED) {
            return "redirect:/"; 
        }

        documentService.incrementDownloads(doc.getId());
        
        // Redirect directly to the Supabase S3 URL to trigger download
        return "redirect:" + doc.getStorageUrl();
    }
}
