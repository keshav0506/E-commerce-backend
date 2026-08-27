package com.keshav.controller;

import com.keshav.dto.ProductReviewsSummaryDTO;
import com.keshav.dto.ReviewRequestDTO;
import com.keshav.dto.ReviewResponseDTO;
import com.keshav.service.IReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ProductReviewsSummaryDTO> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponseDTO> addOrUpdateReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addOrUpdateReview(productId, request));
    }

    @DeleteMapping("/products/{productId}/reviews")
    public ResponseEntity<Void> deleteMyReview(@PathVariable Long productId) {
        reviewService.deleteMyReview(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/reviews/{reviewId}")
    public ResponseEntity<Void> adminDeleteReview(@PathVariable Long reviewId) {
        reviewService.adminDeleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
