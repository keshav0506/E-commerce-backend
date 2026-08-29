package com.keshav.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_notifications", indexes = {
        @Index(name = "idx_notif_supplier", columnList = "supplier_id"),
        @Index(name = "idx_notif_read", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 50)
    private String type = "GENERAL"; // PO_NEW, PO_STATUS, ACCOUNT_STATUS, SHIPMENT

    @Column(name = "target_url", length = 200)
    private String targetUrl;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplierNotification(Long supplierId, String title, String message, String type, String targetUrl) {
        this.supplierId = supplierId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.targetUrl = targetUrl;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}
