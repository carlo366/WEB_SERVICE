package com.example.web_service.controller;

import com.example.web_service.dto.Response;
import com.example.web_service.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class FileController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * Upload single image
     */
    @PostMapping("/upload")
    public ResponseEntity<Response<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String filename = fileUploadService.uploadImage(file);

            Map<String, String> data = new HashMap<>();
            data.put("filename", filename);
            data.put("url", "/api/uploads/" + filename);
            data.put("size", String.valueOf(file.getSize()));

            return ResponseEntity.ok(
                    Response.successfulResponse("File uploaded successfully", data)
            );
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.failedResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to upload file"));
        }
    }

    /**
     * Upload image with auto-resize (for optimization)
     */
    @PostMapping("/upload/optimized")
    public ResponseEntity<Response<Map<String, String>>> uploadOptimizedImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxWidth", defaultValue = "1920") int maxWidth) {
        try {
            String filename = fileUploadService.uploadImageWithResize(file, maxWidth);

            Map<String, String> data = new HashMap<>();
            data.put("filename", filename);
            data.put("url", "/api/uploads/" + filename);
            data.put("maxWidth", String.valueOf(maxWidth));

            return ResponseEntity.ok(
                    Response.successfulResponse("File uploaded and optimized successfully", data)
            );
        } catch (IllegalArgumentException e) {
            log.warn("Validation failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Response.failedResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to upload file"));
        }
    }

    /**
     * Get/Serve uploaded file
     */
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            // Prevent path traversal attack
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("Invalid filename requested: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get(fileUploadService.getUploadDir(), filename);
            File file = filePath.toFile();

            if (!file.exists() || !file.isFile()) {
                log.warn("File not found: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Detect content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("Error serving file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete file (Optional - for admin/owner)
     */
    @DeleteMapping("/uploads/{filename}")
    public ResponseEntity<Response<String>> deleteFile(@PathVariable String filename) {
        try {
            boolean deleted = fileUploadService.deleteFile(filename);

            if (deleted) {
                return ResponseEntity.ok(
                        Response.successfulResponse("File deleted successfully", null)
                );
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Response.failedResponse(HttpStatus.NOT_FOUND.value(), "File not found"));
            }
        } catch (Exception e) {
            log.error("Error deleting file: {}", filename, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.failedResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to delete file"));
        }
    }
}
