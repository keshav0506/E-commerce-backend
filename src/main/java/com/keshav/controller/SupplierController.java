package com.keshav.controller;

import com.keshav.dto.*;
import com.keshav.entity.PurchaseOrderStatus;
import com.keshav.entity.SupplierProfile;
import com.keshav.entity.WholesaleQuoteRequest;
import com.keshav.exception.ResourceNotFoundException;
import com.keshav.repository.SupplierProfileRepository;
import com.keshav.repository.WholesaleQuoteRepository;
import com.keshav.service.EmailService;
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
    private final WholesaleQuoteRepository wholesaleQuoteRepository;
    private final SupplierProfileRepository supplierProfileRepository;
    private final EmailService emailService;

    public SupplierController(ISupplierService supplierService,
                              IPurchaseOrderService purchaseOrderService,
                              WholesaleQuoteRepository wholesaleQuoteRepository,
                              SupplierProfileRepository supplierProfileRepository,
                              EmailService emailService) {
        this.supplierService = supplierService;
        this.purchaseOrderService = purchaseOrderService;
        this.wholesaleQuoteRepository = wholesaleQuoteRepository;
        this.supplierProfileRepository = supplierProfileRepository;
        this.emailService = emailService;
    }

    // ==========================================
    // PUBLIC SUPPLIER ONBOARDING / APPLICATION
    // ==========================================

    @PostMapping("/suppliers/apply")
    public ResponseEntity<SupplierProfileDTO> applySupplier(@Valid @RequestBody SupplierApplyRequestDTO request) {
        SupplierProfileDTO response = supplierService.applySupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/suppliers/{id}/public-catalog")
    public ResponseEntity<SupplierPublicCatalogDTO> getPublicSupplierCatalog(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.getPublicSupplierCatalog(id, search, pageable));
    }

    @PostMapping("/suppliers/{id}/quote")
    public ResponseEntity<WholesaleQuoteResponseDTO> submitWholesaleQuote(
            @PathVariable Long id,
            @Valid @RequestBody WholesaleQuoteRequestDTO request) {

        SupplierProfile supplier = supplierProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        WholesaleQuoteRequest quote = new WholesaleQuoteRequest();
        quote.setSupplier(supplier);
        quote.setCompanyName(request.getCompanyName());
        quote.setContactName(request.getContactName());
        quote.setContactEmail(request.getContactEmail());
        quote.setContactPhone(request.getContactPhone());
        quote.setQuantity(request.getQuantity());
        quote.setNotes(request.getNotes());
        quote.setProductId(request.getProductId());
        quote.setProductName(request.getProductName());
        quote.setStatus("PENDING");

        WholesaleQuoteRequest saved = wholesaleQuoteRepository.save(quote);
        String referenceId = "WQ-" + saved.getId();

        // Fire async email notification to supplier + admin
        emailService.sendWholesaleQuoteNotification(
                referenceId,
                supplier.getBusinessName(),
                supplier.getBusinessEmail(),
                request.getCompanyName(),
                request.getContactName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getQuantity(),
                request.getProductName(),
                request.getNotes()
        );

        WholesaleQuoteResponseDTO response = WholesaleQuoteResponseDTO.builder()
                .id(saved.getId())
                .referenceId(referenceId)
                .supplierBusinessName(supplier.getBusinessName())
                .companyName(saved.getCompanyName())
                .contactName(saved.getContactName())
                .contactEmail(saved.getContactEmail())
                .quantity(saved.getQuantity())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .message("Your wholesale quote request has been submitted successfully! " +
                         supplier.getBusinessName() + " will contact you within 1-2 business days.")
                .build();

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

    // ==========================================
    // SUPPLIER PRODUCT CATALOG MANAGEMENT
    // ==========================================

    @GetMapping("/supplier/products")
    public ResponseEntity<Page<SupplierProductDTO>> getMyProducts(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(supplierService.getMyProducts(search, pageable));
    }

    @PostMapping("/supplier/products")
    public ResponseEntity<SupplierProductDTO> createProduct(@Valid @RequestBody SupplierProductRequestDTO request) {
        SupplierProductDTO created = supplierService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/supplier/products/{id}")
    public ResponseEntity<SupplierProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getMyProductById(id));
    }

    @PutMapping("/supplier/products/{id}")
    public ResponseEntity<SupplierProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody SupplierProductRequestDTO request) {
        return ResponseEntity.ok(supplierService.updateProduct(id, request));
    }

    @DeleteMapping("/supplier/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        supplierService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/supplier/products/{id}/stock")
    public ResponseEntity<SupplierProductDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody SupplierStockUpdateDTO request) {
        return ResponseEntity.ok(supplierService.updateProductStock(id, request.getStock()));
    }
}
