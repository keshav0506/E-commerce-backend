package com.keshav.controller;

import com.keshav.dto.*;
import com.keshav.entity.PurchaseOrderStatus;
import com.keshav.service.IPurchaseOrderService;
import com.keshav.service.ISupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SupplierController {

    private final ISupplierService supplierService;
    private final IPurchaseOrderService purchaseOrderService;

    public SupplierController(ISupplierService supplierService, IPurchaseOrderService purchaseOrderService) {
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
    }

    // ==========================================
    // PUBLIC SUPPLIER ONBOARDING / APPLICATION
    // ==========================================

    @PostMapping("/suppliers/apply")
    public ResponseEntity<SupplierProfileDTO> applySupplier(@Valid @RequestBody SupplierApplyRequestDTO request) {
        SupplierProfileDTO response = supplierService.applySupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==========================================
    // SUPPLIER PORTAL (AUTHENTICATED SUPPLIER)
    // ==========================================

    @GetMapping("/supplier/profile")
    public ResponseEntity<SupplierProfileDTO> getMyProfile() {
        return ResponseEntity.ok(supplierService.getMyProfile());
    }

    @PutMapping("/supplier/profile")
    public ResponseEntity<SupplierProfileDTO> updateMyProfile(@RequestBody SupplierProfileDTO request) {
        return ResponseEntity.ok(supplierService.updateMyProfile(request));
    }

    @GetMapping("/supplier/dashboard")
    public ResponseEntity<SupplierDashboardDTO> getDashboard() {
        return ResponseEntity.ok(supplierService.getDashboardMetrics());
    }

    @GetMapping("/supplier/purchase-orders")
    public ResponseEntity<Page<PurchaseOrderDTO>> getPurchaseOrders(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(purchaseOrderService.getSupplierPurchaseOrders(status, search, pageable));
    }

    @GetMapping("/supplier/purchase-orders/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getSupplierPurchaseOrderById(id));
    }

    @PostMapping("/supplier/purchase-orders/{id}/accept")
    public ResponseEntity<PurchaseOrderDTO> acceptPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PurchaseOrderActionDTO request) {
        return ResponseEntity.ok(purchaseOrderService.acceptPurchaseOrder(id, request));
    }

    @PostMapping("/supplier/purchase-orders/{id}/reject")
    public ResponseEntity<PurchaseOrderDTO> rejectPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PurchaseOrderActionDTO request) {
        return ResponseEntity.ok(purchaseOrderService.rejectPurchaseOrder(id, request));
    }

    @PostMapping("/supplier/purchase-orders/{id}/process")
    public ResponseEntity<PurchaseOrderDTO> processPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PurchaseOrderActionDTO request) {
        return ResponseEntity.ok(purchaseOrderService.processPurchaseOrder(id, request));
    }

    @PostMapping("/supplier/purchase-orders/{id}/ship")
    public ResponseEntity<PurchaseOrderDTO> shipPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PurchaseOrderActionDTO request) {
        return ResponseEntity.ok(purchaseOrderService.shipPurchaseOrder(id, request));
    }

    @PostMapping("/supplier/purchase-orders/{id}/deliver")
    public ResponseEntity<PurchaseOrderDTO> deliverPurchaseOrder(
            @PathVariable Long id,
            @RequestBody(required = false) PurchaseOrderActionDTO request) {
        return ResponseEntity.ok(purchaseOrderService.deliverPurchaseOrder(id, request));
    }

    @GetMapping("/supplier/notifications")
    public ResponseEntity<List<SupplierNotificationDTO>> getNotifications() {
        return ResponseEntity.ok(supplierService.getMyNotifications());
    }

    @PutMapping("/supplier/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationRead(@PathVariable Long id) {
        supplierService.markNotificationAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
