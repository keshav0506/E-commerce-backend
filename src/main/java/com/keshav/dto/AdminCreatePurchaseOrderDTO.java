package com.keshav.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminCreatePurchaseOrderDTO {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private LocalDateTime expectedDeliveryDate;

    private String notes;

    @NotEmpty(message = "Items list cannot be empty")
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        private int quantity = 1;

        private BigDecimal unitPrice;
    }
}
