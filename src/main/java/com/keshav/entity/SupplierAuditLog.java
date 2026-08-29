package com.keshav.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_audit_logs", indexes = {
        @Index(name = "idx_audit_supplier", columnList = "supplier_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "actor_email", nullable = false, length = 150)
    private String actorEmail;

    @Column(nullable = false, length = 100)
    private String action; // e.g. SUPPLIER_REGISTERED, SUPPLIER_APPROVED, PO_ACCEPTED, PO_SHIPPED

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SupplierAuditLog(Long supplierId, String actorEmail, String action, String description) {
        this.supplierId = supplierId;
        this.actorEmail = actorEmail;
        this.action = action;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }
}
