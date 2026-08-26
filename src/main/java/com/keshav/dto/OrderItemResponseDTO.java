package com.keshav.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String image;
    private double price;
    private int quantity;
    private double totalPrice;
}