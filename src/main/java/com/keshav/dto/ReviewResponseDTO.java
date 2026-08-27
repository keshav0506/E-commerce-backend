package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private String userEmail;
    private int rating;
    private String title;
    private String comment;
    private boolean verifiedPurchase;
    private boolean isOwner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
