package com.keshav.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductRequestDTO {

    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    private String shortDescription;

    private String brand;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private Double discountPrice;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    private Integer lowStockThreshold = 5;

    @NotBlank(message = "Image URL is required")
    private String image;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String status = "ACTIVE";

    private Boolean featured = false;
}
