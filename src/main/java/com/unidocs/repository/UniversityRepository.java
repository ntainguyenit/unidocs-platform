package com.unidocs.repository;

import com.unidocs.domain.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findBySlug(String slug);
    Optional<University> findByName(String name);
}
