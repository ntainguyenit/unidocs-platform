package com.unidocs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.unidocs.domain.University;
import com.unidocs.repository.FacultyRepository;
import com.unidocs.repository.UniversityRepository;
import java.util.List;

/**
 * Controller xử lý các yêu cầu trang chủ và giao diện chính.
 */
@Controller
public class HomeController {

    /**
     * Hiển thị trang chủ với danh sách tài liệu nổi bật/mới nhất.
     * 
     * @param model Đối tượng Model để truyền dữ liệu sang View
     * @return Tên template Thymeleaf (index)
     */
    private final UniversityRepository universityRepository;
    private final FacultyRepository facultyRepository;
    private final com.unidocs.repository.CourseRepository courseRepository;
    private final com.unidocs.service.DocumentService documentService;
    private final com.unidocs.service.NotificationService notificationService;

    public HomeController(UniversityRepository universityRepository, FacultyRepository facultyRepository, 
                          com.unidocs.repository.CourseRepository courseRepository, com.unidocs.service.DocumentService documentService,
                          com.unidocs.service.NotificationService notificationService) {
        this.universityRepository = universityRepository;
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.documentService = documentService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("universities", universityRepository.findAll());
        model.addAttribute("notifications", notificationService.getRecentNotifications());
        return "index";
    }

    @GetMapping("/university/{slug}")
    public String universityDetail(@org.springframework.web.bind.annotation.PathVariable String slug, Model model) {
        University uni = universityRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Invalid university slug:" + slug));
        
        model.addAttribute("university", uni);
        model.addAttribute("faculties", uni.getFaculties());
        return "university";
    }

    @GetMapping("/faculty/{slug}")
    public String facultyDetail(@org.springframework.web.bind.annotation.PathVariable String slug, Model model) {
        com.unidocs.domain.Faculty faculty = facultyRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Invalid faculty slug:" + slug));
        
        model.addAttribute("faculty", faculty);
        model.addAttribute("courses", faculty.getCourses());
        return "faculty";
    }

    @org.springframework.beans.factory.annotation.Value("${cloudflare.turnstile.site-key:}")
    private String turnstileSiteKey;

    @GetMapping("/course/{slug}")
    public String courseDetail(@org.springframework.web.bind.annotation.PathVariable String slug, Model model) {
        com.unidocs.domain.Course course = courseRepository.findBySlug(slug)
            .orElseThrow(() -> new IllegalArgumentException("Invalid course slug:" + slug));
        
        java.util.List<com.unidocs.domain.Document> allDocs = documentService.getApprovedDocumentsByCourse(course.getId());
            
        model.addAttribute("course", course);
        model.addAttribute("documents", allDocs);
        model.addAttribute("turnstileSiteKey", turnstileSiteKey);
        
        return "course"; // Need to create this template
    }
}
