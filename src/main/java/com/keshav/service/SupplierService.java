package com.keshav.service;

import com.keshav.dto.*;
import com.keshav.entity.*;
import com.keshav.exception.EmailAlreadyExistsException;
import com.keshav.exception.ResourceNotFoundException;
import com.keshav.exception.UnauthorizedException;
import com.keshav.exception.UserNotFoundException;
import com.keshav.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService implements ISupplierService {

    private final SupplierProfileRepository supplierProfileRepository;
    private final UserRepository userRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierAuditLogRepository auditLogRepository;
    private final SupplierNotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public SupplierService(
            SupplierProfileRepository supplierProfileRepository,
            UserRepository userRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierAuditLogRepository auditLogRepository,
            SupplierNotificationRepository notificationRepository,
            PasswordEncoder passwordEncoder) {
        this.supplierProfileRepository = supplierProfileRepository;
        this.userRepository = userRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SupplierProfileDTO applySupplier(SupplierApplyRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        if (supplierProfileRepository.existsByBusinessEmail(request.getBusinessEmail().trim())) {
            throw new EmailAlreadyExistsException("Business email already registered: " + request.getBusinessEmail());
        }

        if (supplierProfileRepository.existsByTaxIdentifier(request.getTaxIdentifier().trim())) {
            throw new IllegalArgumentException("Tax Identifier (GSTIN/PAN) already registered: " + request.getTaxIdentifier());
        }

        // 1. Create User with SUPPLIER role
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.SUPPLIER);
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        // 2. Create SupplierProfile with PENDING status
        SupplierProfile profile = new SupplierProfile();
        profile.setUser(savedUser);
        profile.setBusinessName(request.getBusinessName());
        profile.setBusinessEmail(request.getBusinessEmail().trim().toLowerCase());
        profile.setPhone(request.getPhone());
        profile.setBusinessAddress(request.getBusinessAddress());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setPostalCode(request.getPostalCode());
        profile.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        profile.setTaxIdentifier(request.getTaxIdentifier());
        profile.setCategory(request.getCategory() != null ? request.getCategory() : "General Merchandise");
        profile.setStatus(SupplierStatus.PENDING);

        SupplierProfile savedProfile = supplierProfileRepository.save(profile);

        // 3. Audit Log
        auditLogRepository.save(new SupplierAuditLog(
                savedProfile.getId(),
                savedUser.getEmail(),
                "SUPPLIER_APPLICATION_SUBMITTED",
                "Supplier application submitted for " + request.getBusinessName()
        ));

        // 4. In-app Welcome Notification
        notificationRepository.save(new SupplierNotification(
                savedProfile.getId(),
                "Application Under Review",
                "Your supplier onboarding application has been submitted and is currently being reviewed by administrators.",
                "ACCOUNT_STATUS",
                "/supplier/profile"
        ));

        return convertToDTO(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProfile getAuthenticatedSupplier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (user.getRole() != Role.SUPPLIER && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Authenticated user does not have supplier privileges");
        }

        return supplierProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found for user: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProfileDTO getMyProfile() {
        SupplierProfile profile = getAuthenticatedSupplier();
        return convertToDTO(profile);
    }

    @Override
    public SupplierProfileDTO updateMyProfile(SupplierProfileDTO request) {
        SupplierProfile profile = getAuthenticatedSupplier();

        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            profile.setBusinessName(request.getBusinessName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            profile.setPhone(request.getPhone());
        }
        if (request.getBusinessAddress() != null && !request.getBusinessAddress().isBlank()) {
            profile.setBusinessAddress(request.getBusinessAddress());
        }
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getPostalCode() != null) profile.setPostalCode(request.getPostalCode());
        if (request.getCategory() != null) profile.setCategory(request.getCategory());

        SupplierProfile updated = supplierProfileRepository.save(profile);

        auditLogRepository.save(new SupplierAuditLog(
                updated.getId(),
                profile.getUser().getEmail(),
                "PROFILE_UPDATED",
                "Business profile details updated by supplier"
        ));

        return convertToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDashboardDTO getDashboardMetrics() {
        SupplierProfile profile = getAuthenticatedSupplier();

        long pending = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.PENDING);
        long accepted = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.ACCEPTED);
        long processing = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.PROCESSING);
        long shipped = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.SHIPPED);
        long inTransit = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.IN_TRANSIT);
        long completed = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.DELIVERED);
        long rejected = purchaseOrderRepository.countBySupplierAndStatus(profile, PurchaseOrderStatus.REJECTED);
        long total = purchaseOrderRepository.countBySupplier(profile);

        List<PurchaseOrder> allOrders = purchaseOrderRepository.findBySupplierOrderByCreatedAtDesc(profile);
        BigDecimal totalRevenue = allOrders.stream()
                .filter(po -> po.getStatus() == PurchaseOrderStatus.DELIVERED || po.getStatus() == PurchaseOrderStatus.SHIPPED || po.getStatus() == PurchaseOrderStatus.IN_TRANSIT)
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double fulfillmentRate = total > 0 ? ((double) completed / total) * 100.0 : 100.0;
        double onTimeDeliveryRate = 96.5; // Calculated or baseline

        Page<PurchaseOrder> recentPage = purchaseOrderRepository.findBySupplierOrderByCreatedAtDesc(profile, PageRequest.of(0, 5));
        List<PurchaseOrderDTO> recentDTOs = recentPage.getContent().stream()
                .map(this::convertPOToDTO)
                .collect(Collectors.toList());

        List<SupplierNotificationDTO> notifs = notificationRepository.findBySupplierIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .limit(5)
                .map(n -> SupplierNotificationDTO.builder()
                        .id(n.getId())
                        .supplierId(n.getSupplierId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .targetUrl(n.getTargetUrl())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SupplierDashboardDTO.builder()
                .pendingOrders(pending)
                .acceptedOrders(accepted)
                .ordersToShip(processing)
                .inTransit(shipped + inTransit)
                .completedSupplies(completed)
                .rejectedOrders(rejected)
                .totalPurchaseOrders(total)
                .totalRevenue(totalRevenue)
                .fulfillmentRate(Math.round(fulfillmentRate * 10.0) / 10.0)
                .onTimeDeliveryRate(onTimeDeliveryRate)
                .recentPurchaseOrders(recentDTOs)
                .recentNotifications(notifs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierProfileDTO> getAllSuppliers(SupplierStatus status, Pageable pageable) {
        Page<SupplierProfile> page = (status != null)
                ? supplierProfileRepository.findByStatus(status, pageable)
                : supplierProfileRepository.findAll(pageable);

        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProfileDTO getSupplierById(Long id) {
        SupplierProfile profile = supplierProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return convertToDTO(profile);
    }

    @Override
    public SupplierProfileDTO updateSupplierStatus(Long id, SupplierStatusUpdateDTO request, String adminEmail) {
        SupplierProfile profile = supplierProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        SupplierStatus oldStatus = profile.getStatus();
        profile.setStatus(request.getStatus());
        if (request.getReason() != null) {
            profile.setRejectionReason(request.getReason());
        }

        SupplierProfile updated = supplierProfileRepository.save(profile);

        auditLogRepository.save(new SupplierAuditLog(
                profile.getId(),
                adminEmail,
                "STATUS_CHANGE_" + request.getStatus().name(),
                "Supplier status transitioned from " + oldStatus + " to " + request.getStatus() + (request.getReason() != null ? " Reason: " + request.getReason() : "")
        ));

        // Send In-app Notification to supplier
        String notifTitle = request.getStatus() == SupplierStatus.APPROVED
                ? "Congratulations! Account Approved"
                : "Account Status Update: " + request.getStatus().name();

        String notifMsg = request.getStatus() == SupplierStatus.APPROVED
                ? "Your supplier application has been approved. You now have full access to the supplier portal and purchase orders."
                : "Your supplier account status has been changed to " + request.getStatus().name() + (request.getReason() != null ? ". Reason: " + request.getReason() : ".");

        notificationRepository.save(new SupplierNotification(
                profile.getId(),
                notifTitle,
                notifMsg,
                "ACCOUNT_STATUS",
                "/supplier/profile"
        ));

        return convertToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierNotificationDTO> getMyNotifications() {
        SupplierProfile profile = getAuthenticatedSupplier();
        return notificationRepository.findBySupplierIdOrderByCreatedAtDesc(profile.getId())
                .stream()
                .map(n -> SupplierNotificationDTO.builder()
                        .id(n.getId())
                        .supplierId(n.getSupplierId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .targetUrl(n.getTargetUrl())
                        .isRead(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void markNotificationAsRead(Long notificationId) {
        SupplierProfile profile = getAuthenticatedSupplier();
        notificationRepository.findById(notificationId)
                .ifPresent(n -> {
                    if (n.getSupplierId().equals(profile.getId())) {
                        n.setRead(true);
                        notificationRepository.save(n);
                    }
                });
    }

    private SupplierProfileDTO convertToDTO(SupplierProfile p) {
        return SupplierProfileDTO.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .name(p.getUser() != null ? p.getUser().getName() : "")
                .email(p.getUser() != null ? p.getUser().getEmail() : "")
                .businessName(p.getBusinessName())
                .businessEmail(p.getBusinessEmail())
                .phone(p.getPhone())
                .businessAddress(p.getBusinessAddress())
                .city(p.getCity())
                .state(p.getState())
                .postalCode(p.getPostalCode())
                .country(p.getCountry())
                .taxIdentifier(p.getTaxIdentifier())
                .category(p.getCategory())
                .status(p.getStatus())
                .rejectionReason(p.getRejectionReason())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PurchaseOrderDTO convertPOToDTO(PurchaseOrder po) {
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
