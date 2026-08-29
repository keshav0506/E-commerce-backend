package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
