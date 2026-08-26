package com.keshav.controller;

import com.keshav.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @RequestParam("file") MultipartFile file) {

        String imageUrl =
                imageService.uploadProductImage(file);

        return ResponseEntity.ok(
                Map.of(
                        "imageUrl",
                        imageUrl
                )
        );
    }
}