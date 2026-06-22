package com.unidocs;

import com.unidocs.domain.Faculty;
import com.unidocs.repository.FacultyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CleanupRunner implements CommandLineRunner {

    private final FacultyRepository facultyRepository;

    public CleanupRunner(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- RUNNING CLEANUP SCRIPT ---");
        List<Faculty> faculties = facultyRepository.findAll();
        for (Faculty faculty : faculties) {
            if (faculty.getName().equalsIgnoreCase("Đề thi các năm của trường Đại Học Khoa Học - Đại học Huế")) {
                System.out.println("Deleting faculty: " + faculty.getName());
                facultyRepository.delete(faculty);
            }
        }
        System.out.println("--- CLEANUP SCRIPT DONE ---");
    }
}
