package com.unidocs.repository;

import com.unidocs.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
    
    List<Course> findByNameContainingIgnoreCase(String keyword);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c LEFT JOIN FETCH c.faculty")
    List<Course> findAllWithFaculty();
}
