package com.example.web_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "webp"
    );

    /**
     * Upload image with validation and optimization
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Validations
        validateFile(file);
        validateImageContent(file);

        // Create upload directory if not exists
        createUploadDirectoryIfNotExists();

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // Save file
        Path filePath = Paths.get(uploadDir, newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded successfully: {}", newFilename);
        return newFilename;
    }

    /**
     * Upload image with resize (for optimization)
     */
    public String uploadImageWithResize(MultipartFile file, int maxWidth) throws IOException {
        // Validations
        validateFile(file);
        validateImageContent(file);

        // Create upload directory if not exists
        createUploadDirectoryIfNotExists();

        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("Cannot read image file");
        }

        // Resize if needed
        BufferedImage resizedImage = resizeImage(originalImage, maxWidth);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // Save resized image
        Path filePath = Paths.get(uploadDir, newFilename);
        ImageIO.write(resizedImage, extension, filePath.toFile());

        log.info("Image uploaded and resized successfully: {}", newFilename);
        return newFilename;
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                return false;
            }

            // Prevent path traversal attack
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("Invalid filename attempted to delete: {}", filename);
                return false;
            }

            Path filePath = Paths.get(uploadDir, filename);
            File file = filePath.toFile();

            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("File deleted successfully: {}", filename);
                }
                return deleted;
            }

            return false;
        } catch (Exception e) {
            log.error("Error deleting file: {}", filename, e);
            return false;
        }
    }

    /**
     * Validate file basic requirements
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are allowed");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Only jpg, jpeg, png, and webp are allowed");
        }

        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    /**
     * Validate actual image content (prevent fake extensions)
     */
    private void validateImageContent(MultipartFile file) throws IOException {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }

            // Check image dimensions
            int width = image.getWidth();
            int height = image.getHeight();

            if (width < 50 || height < 50) {
                throw new IllegalArgumentException("Image dimensions too small (minimum 50x50)");
            }

            if (width > 10000 || height > 10000) {
                throw new IllegalArgumentException("Image dimensions too large (maximum 10000x10000)");
            }

        } catch (IOException e) {
            log.error("Error validating image content", e);
            throw new IllegalArgumentException("Cannot read image file. File may be corrupted.");
        }
    }

    /**
     * Resize image while maintaining aspect ratio
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Don't resize if image is already smaller
        if (originalWidth <= maxWidth) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        int newWidth = maxWidth;
        int newHeight = (originalHeight * maxWidth) / originalWidth;

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();

        // Better quality resizing
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        return resizedImage;
    }

    /**
     * Create upload directory if not exists
     */
    private void createUploadDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Upload directory created: {}", uploadDir);
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Get upload directory path
     */
    public String getUploadDir() {
        return uploadDir;
    }
}
