package com.keshav.dto;

import com.keshav.entity.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {

    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierBusinessName;
    private String supplierEmail;
    private PurchaseOrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private BigDecimal totalAmount;
    private String shippingCarrier;
    private String trackingNumber;
    private String supplierNotes;
    private String rejectionReason;
    private int totalItemsCount;
    private List<PurchaseOrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
