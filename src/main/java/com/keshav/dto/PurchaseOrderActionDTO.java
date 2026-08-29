package com.keshav.dto;

import lombok.Data;

@Data
public class PurchaseOrderActionDTO {

    private String notes;
    private String reason;
    private String carrier;
    private String trackingNumber;
}
