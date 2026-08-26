package com.keshav.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long cartId;
    private List<CartItemResponseDTO> items;
    private double totalAmount;
}