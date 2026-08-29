package com.keshav.service;

import com.keshav.dto.ProductReviewsSummaryDTO;
import com.keshav.dto.ReviewRequestDTO;
import com.keshav.dto.ReviewResponseDTO;
import com.keshav.entity.Product;
import com.keshav.entity.Review;
import com.keshav.entity.User;
import com.keshav.exception.ProductNotFoundException;
import com.keshav.exception.ReviewNotFoundException;
import com.keshav.exception.UserNotFoundException;
import com.keshav.repository.OrderItemRepository;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.ReviewRepository;
import com.keshav.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /** Serialize List<String> images to JSON string for DB storage */
    private String serializeImages(List<String> images) {
        if (images == null || images.isEmpty()) return null;
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String img : images) {
            if (img == null || img.isBlank()) continue;
            if (!first) sb.append(",");
            sb.append("\"").append(img.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /** Deserialize JSON string from DB back to List<String> */
    private List<String> deserializeImages(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isEmpty()) return new ArrayList<>();

        List<String> list = new ArrayList<>();
        if (trimmed.contains("\"")) {
            boolean inQuotes = false;
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (c == '\"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    String item = current.toString().trim();
                    if (!item.isEmpty()) list.add(unescape(item));
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            String item = current.toString().trim();
            if (!item.isEmpty()) list.add(unescape(item));
        } else if (trimmed.contains(",")) {
            for (String part : trimmed.split(",")) {
                String s = part.trim();
                if (!s.isEmpty()) list.add(s);
            }
        } else {
            list.add(trimmed);
        }
        return list;
    }

    private String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    private User getCurrentUserOrThrow() {
        return getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException("User is not authenticated"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsSummaryDTO getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        Optional<User> currentUserOpt = getAuthenticatedUser();
        Long currentUserId = currentUserOpt.map(User::getId).orElse(null);

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        double sumRating = 0;
        ReviewResponseDTO userReviewDTO = null;

        List<ReviewResponseDTO> dtoList = new ArrayList<>();
        for (Review r : reviews) {
            sumRating += r.getRating();
            distribution.put(r.getRating(), distribution.getOrDefault(r.getRating(), 0L) + 1);

            boolean isOwner = currentUserId != null && currentUserId.equals(r.getUser().getId());
            ReviewResponseDTO dto = mapToResponseDTO(r, isOwner);
            dtoList.add(dto);

            if (isOwner) {
                userReviewDTO = dto;
            }
        }

        int totalReviews = reviews.size();
        double avgRating = 0.0;
        if (totalReviews > 0) {
            avgRating = BigDecimal.valueOf(sumRating / totalReviews)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else {
            avgRating = 4.8; // Default initial display rating
        }

        Map<Integer, Integer> percentages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            long count = distribution.getOrDefault(i, 0L);
            int pct = totalReviews > 0 ? (int) Math.round(((double) count / totalReviews) * 100) : (i == 5 ? 75 : i == 4 ? 20 : 5);
            percentages.put(i, pct);
        }

        ProductReviewsSummaryDTO summary = new ProductReviewsSummaryDTO();
        summary.setProductId(productId);
        summary.setAverageRating(avgRating);
        summary.setTotalReviews(totalReviews);
        summary.setRatingDistribution(distribution);
        summary.setRatingPercentages(percentages);
        summary.setReviews(dtoList);
        summary.setUserHasReviewed(userReviewDTO != null);
        summary.setUserReview(userReviewDTO);

        return summary;
    }

    @Override
    public ReviewResponseDTO addOrUpdateReview(Long productId, ReviewRequestDTO request) {
        User user = getCurrentUserOrThrow();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        boolean isVerified = orderItemRepository.existsByOrderUserIdAndProductId(user.getId(), product.getId());
        String imagesJson = serializeImages(request.getImages());

        Optional<Review> existingReviewOpt = reviewRepository.findByProductIdAndUserId(productId, user.getId());
        Review review;

        if (existingReviewOpt.isPresent()) {
            review = existingReviewOpt.get();
            review.setRating(request.getRating());
            review.setTitle(request.getTitle());
            review.setComment(request.getComment());
            review.setVerifiedPurchase(isVerified);
            review.setImages(imagesJson);
            review.setUpdatedAt(LocalDateTime.now());
        } else {
            review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setRating(request.getRating());
            review.setTitle(request.getTitle());
            review.setComment(request.getComment());
            review.setVerifiedPurchase(isVerified);
            review.setImages(imagesJson);
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
        }

        Review saved = reviewRepository.save(review);
        return mapToResponseDTO(saved, true);
    }

    @Override
    public void deleteMyReview(Long productId) {
        User user = getCurrentUserOrThrow();
        reviewRepository.deleteByProductIdAndUserId(productId, user.getId());
    }

    @Override
    public void adminDeleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    private ReviewResponseDTO mapToResponseDTO(Review r, boolean isOwner) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(r.getId());
        dto.setProductId(r.getProduct().getId());
        dto.setUserId(r.getUser().getId());
        dto.setUserName(r.getUser().getName() != null ? r.getUser().getName() : "Customer");
        dto.setUserEmail(r.getUser().getEmail());
        dto.setRating(r.getRating());
        dto.setTitle(r.getTitle() != null ? r.getTitle() : "Great product!");
        dto.setComment(r.getComment());
        dto.setVerifiedPurchase(r.isVerifiedPurchase());
        dto.setOwner(isOwner);
        dto.setImages(deserializeImages(r.getImages()));
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
