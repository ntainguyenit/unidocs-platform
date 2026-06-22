package com.unidocs.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * Uploads a file to storage and returns its public URL
     * @param file The file to upload
     * @param filename The generated filename to save as
     * @return Public URL of the uploaded file
     */
    String uploadFile(MultipartFile file, String filename);

    /**
     * Uploads a byte array to storage and returns its public URL
     * @param bytes The file content
     * @param filename The generated filename to save as
     * @param contentType The content type of the file
     * @return Public URL of the uploaded file
     */
    String uploadFile(byte[] bytes, String filename, String contentType);

    /**
     * Uploads a java.io.File to storage and returns its public URL
     * @param file The file to upload
     * @param filename The generated filename to save as
     * @param contentType The content type of the file
     * @return Public URL of the uploaded file
     */
    String uploadFile(java.io.File file, String filename, String contentType);

    /**
     * Deletes a file from storage
     * @param filename The filename to delete
     */
    void deleteFile(String filename);
}
