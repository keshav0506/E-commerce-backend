package com.keshav.service;

import com.keshav.dto.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {

    OrderResponseDTO createOrder();

    Page<OrderResponseDTO> getMyOrders(Pageable pageable);

    OrderResponseDTO getMyOrderById(Long id);

    void cancelOrder(Long id);
}