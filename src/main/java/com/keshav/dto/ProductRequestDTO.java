package com.keshav.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String slug;

    private String description;

    private String shortDescription;

    private String brand;

    @PositiveOrZero(message = "Price cannot be negative")
    private double price;

    private Double discountPrice;

    @PositiveOrZero(message = "Stock cannot be negative")
    private int stock;

    private Integer lowStockThreshold;

    private String image;

    private String status;

    private Double rating;

    private Integer reviewCount;

    private Boolean featured;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
