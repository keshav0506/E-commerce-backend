package com.keshav.service;

import com.keshav.dto.OrderItemResponseDTO;
import com.keshav.dto.OrderResponseDTO;
import com.keshav.entity.*;
import com.keshav.exception.CartEmptyException;
import com.keshav.exception.InsufficientStockException;
import com.keshav.exception.OrderNotFoundException;
import com.keshav.repository.CartRepository;
import com.keshav.repository.OrderRepository;
import com.keshav.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartEmptyException(
                                "Cart is empty"
                        )
                );

        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException(
                    "Cannot create order with empty cart"
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());

        double totalAmount = 0;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            // Check stock
            if (product.getStock() < cartItem.getQuantity()) {

                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            // Deduct stock
            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            // Save price at purchase time
            orderItem.setPrice(
                    product.getPrice()
            );

            double itemTotal =
                    product.getPrice()
                            * cartItem.getQuantity();

            totalAmount += itemTotal;

            order.getItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder =
                orderRepository.save(order);

        cart.getItems().clear();

        cartRepository.save(cart);

        return convertToResponseDTO(savedOrder);
    }

    @Override
    public Page<OrderResponseDTO> getMyOrders(
            Pageable pageable) {

        User user = getCurrentUser();

        return orderRepository
                .findByUser(user, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    public OrderResponseDTO getMyOrderById(Long id) {

        User user = getCurrentUser();

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        if (!order.getUser().getId()
                .equals(user.getId())) {

            throw new OrderNotFoundException(
                    "Order not found with id: " + id
            );
        }

        return convertToResponseDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {

        User user = getCurrentUser();

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        if (!order.getUser().getId()
                .equals(user.getId())) {

            throw new OrderNotFoundException(
                    "Order not found with id: " + id
            );
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Order cannot be cancelled"
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private OrderResponseDTO convertToResponseDTO(
            Order order) {

        List<OrderItemResponseDTO> items =
                order.getItems()
                        .stream()
                        .map(this::convertItemToDTO)
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponseDTO convertItemToDTO(
            OrderItem item) {

        Product product = item.getProduct();

        double totalPrice =
                item.getPrice()
                        * item.getQuantity();

        return new OrderItemResponseDTO(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getImage(),
                item.getPrice(),
                item.getQuantity(),
                totalPrice
        );
    }
}