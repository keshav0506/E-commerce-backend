package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> images = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
