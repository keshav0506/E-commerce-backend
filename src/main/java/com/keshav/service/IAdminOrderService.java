package com.keshav.service;

import com.keshav.dto.OrderResponseDTO;
import com.keshav.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAdminOrderService {

    Page<OrderResponseDTO> getAllOrders(
            OrderStatus status,
            Pageable pageable
    );

    OrderResponseDTO getOrderById(Long id);

    OrderResponseDTO updateOrderStatus(
            Long id,
            OrderStatus status
    );
}