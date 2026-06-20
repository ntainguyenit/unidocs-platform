package com.unidocs.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType fileType;

    @Column(nullable = false)
    private Long fileSize; // Size in bytes

    @Column(name = "storage_url", nullable = false, columnDefinition = "text")
    private String storageUrl;

    @Column(name = "thumbnail_url", columnDefinition = "text")
    private String thumbnailUrl;

    @Column(name = "folder_name")
    private String folderName;

    @Column(nullable = false, unique = true)
    private String sha256Hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false)
    private int downloads = 0;

    @Column(nullable = false)
    private String uploaderIp;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "upload_batch_id")
    private String uploadBatchId;

    @Column(name = "reliability_score", nullable = false, columnDefinition = "integer default 100")
    private int reliabilityScore = 100;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
