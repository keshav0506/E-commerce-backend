package com.keshav.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponseDTO {

    private String razorpayOrderId;

    private String keyId;

    private Long orderId;

    private double amount;
}