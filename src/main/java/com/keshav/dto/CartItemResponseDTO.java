package com.keshav.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private double price;
    private String image;
    private int quantity;
    private double totalPrice;
}