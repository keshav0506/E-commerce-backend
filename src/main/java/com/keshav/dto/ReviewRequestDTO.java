package com.keshav.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private int rating;

    private String title;

    @NotBlank(message = "Review comment cannot be empty")
    private String comment;

    /**
     * Optional list of images (base64 data URLs or public URLs).
     * Up to 4 images, each max 5 MB when base64-encoded.
     */
    private List<String> images = new ArrayList<>();
}
