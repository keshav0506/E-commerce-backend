package com.keshav.controller;

import com.keshav.dto.OrderResponseDTO;
import com.keshav.entity.OrderStatus;
import com.keshav.service.IAdminOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final IAdminOrderService adminOrderService;

    public AdminOrderController(
            IAdminOrderService adminOrderService) {

        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(required = false)
            OrderStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                adminOrderService.getAllOrders(
                        status,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminOrderService.getOrderById(id)
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(
                adminOrderService.updateOrderStatus(
                        id,
                        status
                )
        );
    }
}