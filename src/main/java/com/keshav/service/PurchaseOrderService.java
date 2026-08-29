package com.keshav.service;

import com.keshav.dto.*;
import com.keshav.entity.*;
import com.keshav.exception.ResourceNotFoundException;
import com.keshav.exception.UnauthorizedException;
import com.keshav.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PurchaseOrderService implements IPurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierProfileRepository supplierProfileRepository;
    private final ProductRepository productRepository;
    private final ISupplierService supplierService;
    private final SupplierAuditLogRepository auditLogRepository;
    private final SupplierNotificationRepository notificationRepository;

    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            SupplierProfileRepository supplierProfileRepository,
            ProductRepository productRepository,
            ISupplierService supplierService,
            SupplierAuditLogRepository auditLogRepository,
            SupplierNotificationRepository notificationRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierProfileRepository = supplierProfileRepository;
        this.productRepository = productRepository;
        this.supplierService = supplierService;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getSupplierPurchaseOrders(PurchaseOrderStatus status, String search, Pageable pageable) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        Page<PurchaseOrder> page;

        if (status != null) {
            page = purchaseOrderRepository.findBySupplierAndStatusOrderByCreatedAtDesc(supplier, status, pageable);
        } else {
            page = purchaseOrderRepository.findBySupplierOrderByCreatedAtDesc(supplier, pageable);
        }

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            List<PurchaseOrderDTO> filtered = page.getContent().stream()
                    .filter(po -> po.getPoNumber().toLowerCase().contains(q) ||
                            po.getItems().stream().anyMatch(i -> i.getProduct().getName().toLowerCase().contains(q)))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getSupplierPurchaseOrderById(Long id) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));
        return convertToDTO(po);
    }

    @Override
    public PurchaseOrderDTO acceptPurchaseOrder(Long id, PurchaseOrderActionDTO request) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));

        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING purchase orders can be accepted. Current status: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.ACCEPTED);
        if (request != null && request.getNotes() != null) {
            po.setSupplierNotes(request.getNotes());
        }

        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PURCHASE_ORDER_ACCEPTED",
                "Purchase order " + po.getPoNumber() + " accepted by supplier"
        ));

        notificationRepository.save(new SupplierNotification(
                supplier.getId(),
                "Order Accepted: " + po.getPoNumber(),
                "You have accepted Purchase Order " + po.getPoNumber() + ". Please proceed with processing and fulfillment.",
                "PO_STATUS",
                "/supplier/purchase-orders/" + po.getId()
        ));

        return convertToDTO(updated);
    }

    @Override
    public PurchaseOrderDTO rejectPurchaseOrder(Long id, PurchaseOrderActionDTO request) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));

        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING purchase orders can be rejected. Current status: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.REJECTED);
        String reason = (request != null && request.getReason() != null) ? request.getReason() : "Supplier declined the order";
        po.setRejectionReason(reason);

        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PURCHASE_ORDER_REJECTED",
                "Purchase order " + po.getPoNumber() + " rejected. Reason: " + reason
        ));

        return convertToDTO(updated);
    }

    @Override
    public PurchaseOrderDTO processPurchaseOrder(Long id, PurchaseOrderActionDTO request) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));

        if (po.getStatus() != PurchaseOrderStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED purchase orders can transition to PROCESSING. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.PROCESSING);
        if (request != null && request.getNotes() != null) {
            po.setSupplierNotes(request.getNotes());
        }

        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PURCHASE_ORDER_PROCESSING",
                "Order " + po.getPoNumber() + " is now in packing & preparation"
        ));

        return convertToDTO(updated);
    }

    @Override
    public PurchaseOrderDTO shipPurchaseOrder(Long id, PurchaseOrderActionDTO request) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));

        if (po.getStatus() != PurchaseOrderStatus.PROCESSING && po.getStatus() != PurchaseOrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be ACCEPTED or PROCESSING before shipping. Current: " + po.getStatus());
        }

        po.setStatus(PurchaseOrderStatus.SHIPPED);
        if (request != null) {
            if (request.getCarrier() != null) po.setShippingCarrier(request.getCarrier());
            if (request.getTrackingNumber() != null) po.setTrackingNumber(request.getTrackingNumber());
            if (request.getNotes() != null) po.setSupplierNotes(request.getNotes());
        }

        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PURCHASE_ORDER_SHIPPED",
                "Order " + po.getPoNumber() + " shipped via " + po.getShippingCarrier() + " (Tracking: " + po.getTrackingNumber() + ")"
        ));

        notificationRepository.save(new SupplierNotification(
                supplier.getId(),
                "Shipment Dispatched: " + po.getPoNumber(),
                "Tracking #" + po.getTrackingNumber() + " via " + (po.getShippingCarrier() != null ? po.getShippingCarrier() : "Standard Freight"),
                "SHIPMENT",
                "/supplier/shipments"
        ));

        return convertToDTO(updated);
    }

    @Override
    public PurchaseOrderDTO deliverPurchaseOrder(Long id, PurchaseOrderActionDTO request) {
        SupplierProfile supplier = supplierService.getAuthenticatedSupplier();
        PurchaseOrder po = purchaseOrderRepository.findByIdAndSupplier(id, supplier)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found or unauthorized: " + id));

        po.setStatus(PurchaseOrderStatus.DELIVERED);
        po.setActualDeliveryDate(LocalDateTime.now());
        if (request != null && request.getNotes() != null) {
            po.setSupplierNotes(request.getNotes());
        }

        // Restock warehouse product quantities accordingly
        for (PurchaseOrderItem item : po.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        PurchaseOrder updated = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PURCHASE_ORDER_DELIVERED",
                "Order " + po.getPoNumber() + " delivered and warehouse stock replenished"
        ));

        notificationRepository.save(new SupplierNotification(
                supplier.getId(),
                "Fulfillment Completed: " + po.getPoNumber(),
                "Purchase order items received and verified at fulfillment center.",
                "PO_STATUS",
                "/supplier/invoices"
        ));

        return convertToDTO(updated);
    }

    @Override
    public PurchaseOrderDTO createPurchaseOrder(AdminCreatePurchaseOrderDTO request, String adminEmail) {
        SupplierProfile supplier = supplierProfileRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        if (supplier.getStatus() != SupplierStatus.APPROVED) {
            throw new IllegalStateException("Cannot create purchase order for unapproved supplier. Status: " + supplier.getStatus());
        }

        String poNumber = "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + (1000 + (int)(Math.random() * 9000));

        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(poNumber);
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.PENDING);
        po.setOrderDate(LocalDateTime.now());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate() != null
                ? request.getExpectedDeliveryDate()
                : LocalDateTime.now().plusDays(7));
        po.setSupplierNotes(request.getNotes());

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderItem> items = new ArrayList<>();

        for (AdminCreatePurchaseOrderDTO.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemReq.getProductId()));

            BigDecimal unitPrice = itemReq.getUnitPrice() != null
                    ? itemReq.getUnitPrice()
                    : BigDecimal.valueOf(product.getPrice() * 0.75); // B2B wholesale baseline price

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(subtotal);

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            items.add(item);
        }

        po.setTotalAmount(total);
        po.setItems(items);

        PurchaseOrder saved = purchaseOrderRepository.save(po);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                adminEmail,
                "PURCHASE_ORDER_ISSUED",
                "Admin issued new purchase order " + poNumber + " (Total: ₹" + total + ")"
        ));

        notificationRepository.save(new SupplierNotification(
                supplier.getId(),
                "New Purchase Order: " + poNumber,
                "A new purchase order of ₹" + total + " has been issued for your business. Please review and accept.",
                "PO_NEW",
                "/supplier/purchase-orders/" + saved.getId()
        ));

        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getAllPurchaseOrdersAdmin(PurchaseOrderStatus status, Long supplierId, Pageable pageable) {
        Page<PurchaseOrder> page;
        if (status != null) {
            page = purchaseOrderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = purchaseOrderRepository.findAll(pageable);
        }

        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderByIdAdmin(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return convertToDTO(po);
    }

    private PurchaseOrderDTO convertToDTO(PurchaseOrder po) {
        List<PurchaseOrderItemDTO> items = po.getItems().stream()
                .map(item -> PurchaseOrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .productImage(item.getProduct().getImage())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return PurchaseOrderDTO.builder()
                .id(po.getId())
                .poNumber(po.getPoNumber())
                .supplierId(po.getSupplier().getId())
                .supplierBusinessName(po.getSupplier().getBusinessName())
                .supplierEmail(po.getSupplier().getBusinessEmail())
                .status(po.getStatus())
                .orderDate(po.getOrderDate())
                .expectedDeliveryDate(po.getExpectedDeliveryDate())
                .actualDeliveryDate(po.getActualDeliveryDate())
                .totalAmount(po.getTotalAmount())
                .shippingCarrier(po.getShippingCarrier())
                .trackingNumber(po.getTrackingNumber())
                .supplierNotes(po.getSupplierNotes())
                .rejectionReason(po.getRejectionReason())
                .totalItemsCount(po.getItems().size())
                .items(items)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }
}
