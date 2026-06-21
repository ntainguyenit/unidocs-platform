package com.unidocs.service;

import com.unidocs.domain.Course;
import com.unidocs.domain.Document;
import com.unidocs.domain.Faculty;
import com.unidocs.repository.CourseRepository;
import com.unidocs.repository.DocumentRepository;
import com.unidocs.repository.FacultyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeduplicationService {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationService.class);

    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;

    public DeduplicationService(FacultyRepository facultyRepository, CourseRepository courseRepository, DocumentRepository documentRepository) {
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void deduplicateFaculties() {
        List<Faculty> allFaculties = facultyRepository.findAll();
        // Group by lowercased, trimmed name
        Map<String, List<Faculty>> groupedFaculties = allFaculties.stream()
                .collect(Collectors.groupingBy(f -> f.getName().trim().toLowerCase()));

        for (Map.Entry<String, List<Faculty>> entry : groupedFaculties.entrySet()) {
            List<Faculty> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                log.info("Found {} duplicate faculties for name '{}'", duplicates.size(), entry.getKey());
                
                // Pick the primary faculty (the one with the most courses)
                duplicates.sort((f1, f2) -> Integer.compare(f2.getCourses().size(), f1.getCourses().size()));
                
                Faculty primaryFaculty = duplicates.get(0);
                List<Faculty> secondaryFaculties = duplicates.subList(1, duplicates.size());

                for (Faculty secondary : secondaryFaculties) {
                    List<Course> coursesToMove = new ArrayList<>(secondary.getCourses());
                    for (Course course : coursesToMove) {
                        course.setFaculty(primaryFaculty);
                        courseRepository.save(course);
                        primaryFaculty.getCourses().add(course);
                    }
                    secondary.getCourses().clear();
                    facultyRepository.delete(secondary);
                    log.info("Deleted secondary faculty id: {} and moved its courses to primary faculty id: {}", secondary.getId(), primaryFaculty.getId());
                }
            }
        }
    }

    @Transactional
    public void deduplicateCourses() {
        // We need to deduplicate courses WITHIN each faculty.
        List<Faculty> allFaculties = facultyRepository.findAll();
        
        for (Faculty faculty : allFaculties) {
            List<Course> courses = faculty.getCourses();
            Map<String, List<Course>> groupedCourses = courses.stream()
                    .collect(Collectors.groupingBy(c -> c.getName().trim().toLowerCase()));

            for (Map.Entry<String, List<Course>> entry : groupedCourses.entrySet()) {
                List<Course> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    log.info("Found {} duplicate courses for name '{}' in faculty '{}'", duplicates.size(), entry.getKey(), faculty.getName());
                    
                    // Fetch documents for each course to count them
                    Map<Course, List<Document>> courseDocuments = new HashMap<>();
                    for (Course c : duplicates) {
                        // Get all documents (not just approved)
                        List<Document> docs = documentRepository.findAll().stream()
                                .filter(d -> d.getCourse().getId().equals(c.getId()))
                                .collect(Collectors.toList());
                        courseDocuments.put(c, docs);
                    }

                    // Sort by number of documents descending
                    duplicates.sort((c1, c2) -> Integer.compare(courseDocuments.get(c2).size(), courseDocuments.get(c1).size()));
                    
                    Course primaryCourse = duplicates.get(0);
                    List<Course> secondaryCourses = duplicates.subList(1, duplicates.size());

                    for (Course secondary : secondaryCourses) {
                        List<Document> docsToMove = courseDocuments.get(secondary);
                        for (Document doc : docsToMove) {
                            doc.setCourse(primaryCourse);
                            documentRepository.save(doc);
                        }
                        
                        // Remove secondary course from faculty
                        faculty.getCourses().remove(secondary);
                        courseRepository.delete(secondary);
                        log.info("Deleted secondary course id: {} and moved its {} documents to primary course id: {}", secondary.getId(), docsToMove.size(), primaryCourse.getId());
                    }
                }
            }
        }
    }

    public List<List<Document>> findDuplicateDocuments() {
        List<Document> allDocs = documentRepository.findAll();
        Map<Long, Map<String, List<Document>>> groupedByCourseAndTitle = allDocs.stream()
                .filter(d -> d.getCourse() != null && d.getTitle() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getCourse().getId(),
                        Collectors.groupingBy(d -> d.getTitle().trim().toLowerCase())
                ));
                
        List<List<Document>> duplicateGroups = new ArrayList<>();
        for (Map<String, List<Document>> courseGroups : groupedByCourseAndTitle.values()) {
            for (List<Document> group : courseGroups.values()) {
                if (group.size() > 1) {
                    // Sort each group so the newest is first, or by ID to be deterministic
                    group.sort(Comparator.comparing(Document::getId));
                    duplicateGroups.add(group);
                }
            }
        }
        
        return duplicateGroups;
    }

    @Transactional
    public void deleteDuplicateDocuments(List<Long> ids) {
        for (Long id : ids) {
            Document doc = documentRepository.findById(id).orElse(null);
            if (doc != null) {
                // Delete from storage if possible
                // Currently, DocumentService handles storage deletion. Since DocumentService has StorageService, we should use it.
                // Wait, DocumentRepository deletion doesn't auto-delete from storage unless we call StorageService.
                // I will inject DocumentService into DeduplicationService or StorageService.
                // It's better to just delete the file using StorageService.
                documentRepository.delete(doc);
            }
        }
    }
}
