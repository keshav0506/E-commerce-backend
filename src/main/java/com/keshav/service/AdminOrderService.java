package com.keshav.service;

import com.keshav.dto.OrderItemResponseDTO;
import com.keshav.dto.OrderResponseDTO;
import com.keshav.entity.Order;
import com.keshav.entity.OrderStatus;
import com.keshav.exception.OrderNotFoundException;
import com.keshav.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOrderService implements IAdminOrderService {

    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Page<OrderResponseDTO> getAllOrders(
            OrderStatus status,
            Pageable pageable) {

        Page<Order> orders;

        if (status != null) {

            orders = orderRepository.findByStatus(
                    status,
                    pageable
            );

        } else {

            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::convertToResponseDTO);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        return convertToResponseDTO(order);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(
            Long id,
            OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        order.setStatus(status);

        Order updatedOrder =
                orderRepository.save(order);

        return convertToResponseDTO(updatedOrder);
    }

    private OrderResponseDTO convertToResponseDTO(
            Order order) {

        List<OrderItemResponseDTO> items =
                order.getItems()
                        .stream()
                        .map(item -> {

                            double totalPrice =
                                    item.getPrice()
                                            * item.getQuantity();

                            return new OrderItemResponseDTO(
                                    item.getId(),
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    item.getProduct().getImage(),
                                    item.getPrice(),
                                    item.getQuantity(),
                                    totalPrice
                            );
                        })
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}