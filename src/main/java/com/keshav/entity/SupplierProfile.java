package com.keshav.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "business_email", nullable = false, length = 150)
    private String businessEmail;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(name = "business_address", columnDefinition = "TEXT", nullable = false)
    private String businessAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country = "India";

    @Column(name = "tax_identifier", length = 50)
    private String taxIdentifier; // GSTIN / PAN / Tax Registration

    @Column(length = 100)
    private String category = "General Merchandise";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierStatus status = SupplierStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = SupplierStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
