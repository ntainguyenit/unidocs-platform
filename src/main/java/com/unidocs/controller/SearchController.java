package com.unidocs.controller;

import com.unidocs.domain.Course;
import com.unidocs.domain.Document;
import com.unidocs.domain.DocumentStatus;
import com.unidocs.repository.CourseRepository;
import com.unidocs.repository.DocumentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SearchController {

    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;

    public SearchController(CourseRepository courseRepository, DocumentRepository documentRepository) {
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
    }

    @GetMapping("/api/search")
    public List<SearchResult> search() {
        List<SearchResult> results = new ArrayList<>();

        // Lấy tất cả Course
        List<Course> courses = courseRepository.findAll();
        for (Course c : courses) {
            results.add(new SearchResult(
                    c.getName(),
                    "/course/" + c.getSlug(),
                    "Học phần - Khoa " + (c.getFaculty() != null ? c.getFaculty().getName() : "")
            ));
        }

        // Lấy tất cả Document đã duyệt
        List<Document> documents = documentRepository.findByStatus(DocumentStatus.APPROVED);
        for (Document d : documents) {
            results.add(new SearchResult(
                    d.getTitle(),
                    "/document/" + d.getId(),
                    "Tài liệu - Học phần: " + (d.getCourse() != null ? d.getCourse().getName() : "")
            ));
        }

        return results;
    }

    public static class SearchResult {
        public String title;
        public String url;
        public String subtitle;

        public SearchResult(String title, String url, String subtitle) {
            this.title = title;
            this.url = url;
            this.subtitle = subtitle;
        }
    }
}
