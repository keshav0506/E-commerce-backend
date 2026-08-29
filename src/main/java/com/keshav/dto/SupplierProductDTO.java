package com.keshav.dto;

import com.keshav.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProductDTO {

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

    private Long supplierId;
    private String supplierBusinessName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SupplierProductDTO fromEntity(Product p) {
        if (p == null) return null;
        SupplierProductDTO dto = new SupplierProductDTO();
        dto.setId(p.getId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setDescription(p.getDescription());
        dto.setShortDescription(p.getShortDescription());
        dto.setBrand(p.getBrand());
        dto.setPrice(p.getPrice());
        dto.setDiscountPrice(p.getDiscountPrice());
        dto.setStock(p.getStock());
        dto.setLowStockThreshold(p.getLowStockThreshold());
        dto.setImage(p.getImage());
        dto.setStatus(p.getStatus());
        dto.setRating(p.getRating());
        dto.setReviewCount(p.getReviewCount());
        dto.setFeatured(p.getFeatured());

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }

        if (p.getSupplier() != null) {
            dto.setSupplierId(p.getSupplier().getId());
            dto.setSupplierBusinessName(p.getSupplier().getBusinessName());
        }

        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
