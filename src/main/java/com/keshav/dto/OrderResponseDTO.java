package com.keshav.dto;

import com.keshav.entity.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private OrderStatus status;
    private double totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDTO> items;
}