package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String description;
    private double price;
    private int stock;
    private String image;
    private String status;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime addedAt;
}
