package com.keshav.service;

import com.keshav.dto.ProductReviewsSummaryDTO;
import com.keshav.dto.ReviewRequestDTO;
import com.keshav.dto.ReviewResponseDTO;

public interface IReviewService {

    ProductReviewsSummaryDTO getProductReviews(Long productId);

    ReviewResponseDTO addOrUpdateReview(Long productId, ReviewRequestDTO request);

    void deleteMyReview(Long productId);

    void adminDeleteReview(Long reviewId);
}
