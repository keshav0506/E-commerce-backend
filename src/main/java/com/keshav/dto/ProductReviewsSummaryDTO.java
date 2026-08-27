package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewsSummaryDTO {

    private Long productId;
    private double averageRating;
    private int totalReviews;
    private Map<Integer, Long> ratingDistribution = new HashMap<>();
    private Map<Integer, Integer> ratingPercentages = new HashMap<>();
    private List<ReviewResponseDTO> reviews = new ArrayList<>();
    private boolean userHasReviewed;
    private ReviewResponseDTO userReview;
}
