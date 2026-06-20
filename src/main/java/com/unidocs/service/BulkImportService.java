package com.unidocs.service;

import com.unidocs.domain.Course;
import com.unidocs.domain.Document;
import com.unidocs.domain.DocumentStatus;
import com.unidocs.domain.DocumentType;
import com.unidocs.domain.Faculty;
import com.unidocs.repository.CourseRepository;
import com.unidocs.repository.DocumentRepository;
import com.unidocs.repository.FacultyRepository;
import com.unidocs.util.SlugGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BulkImportService {

    private static final Logger log = LoggerFactory.getLogger(BulkImportService.class);

    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;

    private final com.unidocs.repository.UniversityRepository universityRepository;

    public BulkImportService(FacultyRepository facultyRepository, 
                             CourseRepository courseRepository, 
                             DocumentRepository documentRepository, 
                             StorageService storageService,
                             com.unidocs.repository.UniversityRepository universityRepository) {
        this.facultyRepository = facultyRepository;
        this.courseRepository = courseRepository;
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.universityRepository = universityRepository;
    }

    @Transactional
    public void importFromZip(MultipartFile zipFile) throws Exception {
        com.unidocs.domain.University defaultUniversity = universityRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No university found in DB to link faculties"));

        java.io.File tempFile = java.io.File.createTempFile("import-", ".zip");
        try {
            zipFile.transferTo(tempFile);

            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(tempFile)) {
                java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    java.util.zip.ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        String filePath = entry.getName();
                        // Normalize separators
                        filePath = filePath.replace("\\", "/");
                        String[] parts = filePath.split("/");
                        
                        if (parts.length >= 2) { 
                            int startIndex = 0;
                            
                            // Detect Root Folder and skip it (e.g. OneDrive exports or wrapper folders)
                            if (parts.length >= 3) {
                                String p0 = parts[0];
                                String p1 = parts[1];
                                if ((!isFacultyPrefix(p0) && isFacultyPrefix(p1)) || p0.toLowerCase().startsWith("onedrive") || p0.toLowerCase().startsWith("export")) {
                                    startIndex = 1;
                                }
                            }
                            
                            String facultyName = parts[startIndex].trim();
                            String courseName = parts.length > startIndex + 1 ? parts[startIndex + 1].trim() : "Khoa Khác";
                            
                            // Extract Folder Name (Academic Year) and keep original file name
                            String folderName = "Khác (Tài liệu không xác định năm)";
                            if (parts.length > startIndex + 2) {
                                folderName = String.join(" - ", java.util.Arrays.copyOfRange(parts, startIndex + 2, parts.length - 1));
                            }
                            String fileName = parts[parts.length - 1].trim();

                            Faculty faculty = upsertFaculty(facultyName, defaultUniversity);
                            Course course = upsertCourse(courseName, faculty);
                            
                            try (java.io.InputStream is = zip.getInputStream(entry)) {
                                processDocument(is, fileName, course, folderName);
                            }
                        }
                    }
                }
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private boolean isFacultyPrefix(String str) {
        String lower = str.toLowerCase().trim();
        return lower.startsWith("khoa ") || 
               lower.startsWith("viện ") || 
               lower.startsWith("trường ") || 
               lower.startsWith("bộ môn ") ||
               lower.startsWith("môn học đại cương");
    }

    private Faculty upsertFaculty(String name, com.unidocs.domain.University university) {
        List<Faculty> existingFaculties = facultyRepository.findAll().stream()
                .filter(f -> f.getName().trim().equalsIgnoreCase(name))
                .toList();

        if (!existingFaculties.isEmpty()) {
            return existingFaculties.get(0);
        }

        Faculty newFaculty = new Faculty();
        newFaculty.setName(name);
        newFaculty.setSlug(SlugGenerator.generateSlug(name) + "-" + UUID.randomUUID().toString().substring(0, 4));
        newFaculty.setUniversity(university);
        return facultyRepository.save(newFaculty);
    }

    private Course upsertCourse(String name, Faculty faculty) {
        List<Course> existingCourses = faculty.getCourses().stream()
                .filter(c -> c.getName().trim().equalsIgnoreCase(name))
                .toList();

        if (!existingCourses.isEmpty()) {
            return existingCourses.get(0);
        }

        Course newCourse = new Course();
        newCourse.setName(name);
        newCourse.setSlug(SlugGenerator.generateSlug(name) + "-" + UUID.randomUUID().toString().substring(0, 4));
        newCourse.setFaculty(faculty);
        Course savedCourse = courseRepository.save(newCourse);
        faculty.getCourses().add(savedCourse);
        return savedCourse;
    }

    private void processDocument(java.io.InputStream is, String fileName, Course course, String folderName) throws Exception {
        byte[] fileBytes = is.readAllBytes();
        if (fileBytes.length == 0) return;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String sha256Hash = hexString.toString();

        Optional<Document> existingDoc = documentRepository.findBySha256Hash(sha256Hash);
        if (existingDoc.isPresent()) {
            log.info("Document already exists, skipping: {}", fileName);
            return;
        }

        DocumentType fileType = determineType(fileName);
        
        String extension = "";
        int extIndex = fileName.lastIndexOf(".");
        if (extIndex > 0) {
            extension = fileName.substring(extIndex);
        }
        String s3Key = UUID.randomUUID().toString() + extension;
        
        MultipartFile mockFile = new MockMultipartFile(fileBytes, fileName);
        String storageUrl = storageService.uploadFile(mockFile, s3Key);

        String thumbnailUrl = null;
        if (fileType == DocumentType.PDF) {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(fileBytes);
            byte[] thumbBytes = com.unidocs.util.PdfThumbnailUtil.generateThumbnail(bais);
            if (thumbBytes != null) {
                String thumbFilename = "thumb_" + UUID.randomUUID().toString() + ".jpg";
                thumbnailUrl = storageService.uploadFile(thumbBytes, thumbFilename, "image/jpeg");
            }
        }

        Document doc = new Document();
        doc.setTitle(fileName.substring(0, fileName.lastIndexOf(".")));
        doc.setFolderName(folderName);
        doc.setSlug(UUID.randomUUID().toString().substring(0, 8) + "-" + System.currentTimeMillis());
        doc.setFileType(fileType);
        doc.setFileSize((long) fileBytes.length);
        doc.setSha256Hash(sha256Hash);
        doc.setStorageUrl(storageUrl);
        doc.setThumbnailUrl(thumbnailUrl);
        doc.setStatus(DocumentStatus.APPROVED);
        doc.setUploaderIp("ADMIN_IMPORT");
        doc.setUploadedAt(LocalDateTime.now());
        doc.setCourse(course);

        documentRepository.save(doc);

        try {
            Thread.sleep(200); // 200ms delay to prevent rate limits / Cloudflare blocking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private DocumentType determineType(String filename) {
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".pdf")) return DocumentType.PDF;
        if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) return DocumentType.DOCX;
        if (lowerName.matches(".*\\.(png|jpg|jpeg|gif|webp|heic|bmp|svg)$")) return DocumentType.IMAGE;
        return DocumentType.PDF;
    }

    // A simple Mock implementation of MultipartFile for passing to StorageService
    private static class MockMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String name;

        public MockMultipartFile(byte[] content, String name) {
            this.content = content;
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { 
            String lowerName = name.toLowerCase();
            if (lowerName.endsWith(".pdf")) return "application/pdf";
            if (lowerName.endsWith(".png")) return "image/png";
            if (lowerName.matches(".*\\.(jpg|jpeg)$")) return "image/jpeg";
            if (lowerName.endsWith(".gif")) return "image/gif";
            if (lowerName.endsWith(".webp")) return "image/webp";
            if (lowerName.endsWith(".svg")) return "image/svg+xml";
            if (lowerName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            if (lowerName.endsWith(".doc")) return "application/msword";
            return "application/octet-stream"; 
        }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() throws IOException { return content; }
        @Override public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException { throw new UnsupportedOperationException(); }
    }
}
