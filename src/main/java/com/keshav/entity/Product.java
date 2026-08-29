package com.keshav.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    @Column(unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String shortDescription;

    private String brand;

    @Positive(message = "Price must be greater than 0")
    private double price;

    private Double discountPrice;

    @PositiveOrZero(message = "Stock cannot be negative")
    private int stock;

    private Integer lowStockThreshold = 5;

    @NotBlank(message = "Image is required")
    @Column(columnDefinition = "TEXT")
    private String image;

    @NotBlank(message = "Status is required")
    private String status = "ACTIVE";

    private Double rating = 4.5;

    private Integer reviewCount = 0;

    private Boolean featured = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private SupplierProfile supplier;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.rating == null) this.rating = 4.5;
        if (this.reviewCount == null) this.reviewCount = 0;
        if (this.featured == null) this.featured = false;
        if (this.lowStockThreshold == null) this.lowStockThreshold = 5;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}