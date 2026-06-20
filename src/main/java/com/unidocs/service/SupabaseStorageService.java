package com.unidocs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Service
public class SupabaseStorageService implements StorageService {

    @Value("${supabase.s3.endpoint}")
    private String endpoint;

    @Value("${supabase.s3.region}")
    private String region;

    @Value("${supabase.s3.access-key}")
    private String accessKey;

    @Value("${supabase.s3.secret-key}")
    private String secretKey;

    @Value("${supabase.s3.bucket}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (endpoint != null && !endpoint.isEmpty() && accessKey != null && !accessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .checksumValidationEnabled(false) // Fixes Supabase ETag validation errors
                            .build())
                    .build();
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String filename) {
        if (s3Client == null) {
            // For local development without Supabase credentials, return a dummy URL
            return "http://localhost:8080/dummy-storage/" + filename;
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .contentDisposition("inline; filename=\"" + filename + "\"")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // Supabase S3 public URL format:
            // https://<project-ref>.supabase.co/storage/v1/object/public/<bucket>/<filename>
            // We can extract project ref from endpoint if needed, or simply construct it based on endpoint.
            // Endpoint is usually: https://<project-ref>.supabase.co/storage/v1/s3
            String publicUrl = endpoint.replace("/s3", "/object/public/") + bucket + "/" + filename;
            return publicUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Supabase S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(byte[] bytes, String filename, String contentType) {
        if (s3Client == null) {
            return "http://localhost:8080/dummy-storage/" + filename;
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(contentType)
                    .contentDisposition("inline; filename=\"" + filename + "\"")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            
            return endpoint.replace("/s3", "/object/public/") + bucket + "/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload byte array to Supabase S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filename) {
        if (s3Client == null) {
            return; // Dummy mode
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Supabase S3", e);
        }
    }
}
