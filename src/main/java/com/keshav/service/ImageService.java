package com.keshav.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService implements IImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:dummy_cloud}")
    private String cloudName;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Try Cloudinary if real credentials are provided
        if (cloudName != null && !cloudName.isBlank() && !cloudName.equalsIgnoreCase("dummy_cloud")) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "ecommerce/products",
                                "resource_type", "image",
                                "use_filename", true,
                                "unique_filename", true,
                                "overwrite", false
                        )
                );
                return result.get("secure_url").toString();
            } catch (Exception e) {
                log.warn("Cloudinary upload failed ({}), falling back to local file storage: {}", cloudName, e.getMessage());
            }
        }

        // Local Storage Fallback
        try {
            Path uploadDir = Paths.get("uploads/products");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            String extension = "";
            int dotIdx = originalName.lastIndexOf(".");
            if (dotIdx > 0) {
                extension = originalName.substring(dotIdx);
            } else {
                extension = ".jpg";
            }

            String filename = UUID.randomUUID() + extension;
            Path targetLocation = uploadDir.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String localUrl = "http://localhost:8081/uploads/products/" + filename;
            log.info("Saved product image locally: {}", localUrl);
            return localUrl;

        } catch (Exception e) {
            log.error("Failed to save image locally", e);
            throw new RuntimeException("Failed to upload or store image: " + e.getMessage(), e);
        }
    }
}