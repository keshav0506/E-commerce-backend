package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;

    private String sku;

    private String name;

    private String slug;

    private String description;

    private String shortDescription;

    private String brand;

    private double price;

    private Double discountPrice;

    private int stock;

    private Integer lowStockThreshold;

    private String image;

    private String status;

    private Double rating;

    private Integer reviewCount;

    private Boolean featured;

    private Long categoryId;

    private String categoryName;

    private String categorySlug;

    private SupplierSummaryDTO supplier;

    private java.util.Map<String, HateoasLinkDTO> _links;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
