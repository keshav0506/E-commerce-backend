package com.keshav.controller;

import com.keshav.dto.OrderResponseDTO;
import com.keshav.service.IOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder());
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            Pageable pageable) {

        return ResponseEntity.ok(
                orderService.getMyOrders(pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getMyOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getMyOrderById(id)
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id) {

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }
}
