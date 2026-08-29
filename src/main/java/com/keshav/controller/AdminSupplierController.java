package com.keshav.controller;

import com.keshav.dto.*;
import com.keshav.entity.PurchaseOrderStatus;
import com.keshav.entity.SupplierStatus;
import com.keshav.service.IPurchaseOrderService;
import com.keshav.service.ISupplierService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminSupplierController {

    private final ISupplierService supplierService;
    private final IPurchaseOrderService purchaseOrderService;

    public AdminSupplierController(ISupplierService supplierService, IPurchaseOrderService purchaseOrderService) {
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
    }

    // ==========================================
    // SUPPLIER APPLICATION & LIFECYCLE MANAGEMENT
    // ==========================================

    @GetMapping("/suppliers")
    public ResponseEntity<Page<SupplierProfileDTO>> getAllSuppliers(
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.getAllSuppliers(status, pageable));
    }

    @GetMapping("/suppliers/{id}")
    public ResponseEntity<SupplierProfileDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PutMapping("/suppliers/{id}/status")
    public ResponseEntity<SupplierProfileDTO> updateSupplierStatus(
            @PathVariable Long id,
            @Valid @RequestBody SupplierStatusUpdateDTO request,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "admin@system";
        return ResponseEntity.ok(supplierService.updateSupplierStatus(id, request, adminEmail));
    }

    // ==========================================
    // PURCHASE ORDER GENERATION & OVERSIGHT
    // ==========================================

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(
            @Valid @RequestBody AdminCreatePurchaseOrderDTO request,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "admin@system";
        PurchaseOrderDTO response = purchaseOrderService.createPurchaseOrder(request, adminEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<Page<PurchaseOrderDTO>> getAllPurchaseOrders(
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrdersAdmin(status, supplierId, pageable));
    }

    @GetMapping("/purchase-orders/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderByIdAdmin(id));
    }
}
