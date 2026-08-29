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
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductService productService;

    public SupplierService(
            SupplierProfileRepository supplierProfileRepository,
            UserRepository userRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierAuditLogRepository auditLogRepository,
            SupplierNotificationRepository notificationRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            ProductService productService) {
        this.supplierProfileRepository = supplierProfileRepository;
        this.userRepository = userRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.productService = productService;
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

        long totalProducts = productRepository.countBySupplierId(profile.getId());
        long lowStockProducts = productRepository.countBySupplierIdAndStockLessThanEqual(profile.getId(), 5);

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
                .totalProductsListed(totalProducts)
                .lowStockProductsCount(lowStockProducts)
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

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierProductDTO> getMyProducts(String search, Pageable pageable) {
        SupplierProfile supplier = getAuthenticatedSupplier();
        Page<Product> page;
        if (search != null && !search.isBlank()) {
            page = productRepository.findBySupplierIdAndNameContainingIgnoreCase(supplier.getId(), search.trim(), pageable);
        } else {
            page = productRepository.findBySupplierId(supplier.getId(), pageable);
        }
        return page.map(SupplierProductDTO::fromEntity);
    }

    @Override
    public SupplierProductDTO createProduct(SupplierProductRequestDTO request) {
        SupplierProfile supplier = getAuthenticatedSupplier();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Generate SKU if not provided
        String sku = request.getSku();
        if (sku == null || sku.isBlank()) {
            sku = "SKU-" + supplier.getBusinessName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase().substring(0, Math.min(4, supplier.getBusinessName().length())) + "-" + System.currentTimeMillis() % 100000;
        }

        // Generate unique slug
        String baseSlug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        String slug = baseSlug + "-" + (System.currentTimeMillis() % 10000);

        Product product = new Product();
        product.setSku(sku);
        product.setName(request.getName().trim());
        product.setSlug(slug);
        product.setDescription(request.getDescription());
        product.setShortDescription(request.getShortDescription());
        product.setBrand(request.getBrand() != null && !request.getBrand().isBlank() ? request.getBrand() : supplier.getBusinessName());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 5);
        product.setImage(request.getImage());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        product.setRating(4.5);
        product.setReviewCount(0);

        Product saved = productRepository.save(product);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PRODUCT_CREATED",
                "Created product '" + saved.getName() + "' (SKU: " + saved.getSku() + ")"
        ));

        notificationRepository.save(new SupplierNotification(
                supplier.getId(),
                "Product Listed Successfully",
                "Your product '" + saved.getName() + "' has been listed in the catalog.",
                "PRODUCT",
                "/supplier/products"
        ));

        return SupplierProductDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierProductDTO getMyProductById(Long productId) {
        SupplierProfile supplier = getAuthenticatedSupplier();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplier.getId())) {
            throw new UnauthorizedException("You do not have permission to access this product.");
        }

        return SupplierProductDTO.fromEntity(product);
    }

    @Override
    public SupplierProductDTO updateProduct(Long productId, SupplierProductRequestDTO request) {
        SupplierProfile supplier = getAuthenticatedSupplier();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplier.getId())) {
            throw new UnauthorizedException("You do not have permission to update this product.");
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getName() != null && !request.getName().isBlank()) product.setName(request.getName().trim());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getLowStockThreshold() != null) product.setLowStockThreshold(request.getLowStockThreshold());
        if (request.getImage() != null && !request.getImage().isBlank()) product.setImage(request.getImage());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());

        Product updated = productRepository.save(product);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PRODUCT_UPDATED",
                "Updated details for product '" + updated.getName() + "'"
        ));

        return SupplierProductDTO.fromEntity(updated);
    }

    @Override
    public void deleteProduct(Long productId) {
        SupplierProfile supplier = getAuthenticatedSupplier();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplier.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this product.");
        }

        // Soft delete / de-list
        product.setStatus("INACTIVE");
        productRepository.save(product);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "PRODUCT_DELISTED",
                "Delisted product '" + product.getName() + "'"
        ));
    }

    @Override
    public SupplierProductDTO updateProductStock(Long productId, int stock) {
        SupplierProfile supplier = getAuthenticatedSupplier();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplier.getId())) {
            throw new UnauthorizedException("You do not have permission to adjust inventory for this product.");
        }

        int prevStock = product.getStock();
        product.setStock(stock);
        if (stock > 0 && "OUT_OF_STOCK".equalsIgnoreCase(product.getStatus())) {
            product.setStatus("ACTIVE");
        } else if (stock == 0) {
            product.setStatus("OUT_OF_STOCK");
        }

        Product saved = productRepository.save(product);

        auditLogRepository.save(new SupplierAuditLog(
                supplier.getId(),
                supplier.getUser().getEmail(),
                "STOCK_ADJUSTED",
                "Adjusted inventory for '" + saved.getName() + "' from " + prevStock + " to " + stock
        ));

        return SupplierProductDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierPublicCatalogDTO getPublicSupplierCatalog(Long supplierId, String search, Pageable pageable) {
        SupplierProfile supplier = supplierProfileRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + supplierId));

        Page<Product> productsPage;
        if (search != null && !search.trim().isEmpty()) {
            productsPage = productRepository.findBySupplierIdAndNameContainingIgnoreCase(supplierId, search.trim(), pageable);
        } else {
            productsPage = productRepository.findBySupplierId(supplierId, pageable);
        }

        Page<ProductResponseDTO> dtoPage = productsPage.map(productService::convertToResponseDTO);

        SupplierSummaryDTO supplierSummary = SupplierSummaryDTO.builder()
                .id(supplier.getId())
                .businessName(supplier.getBusinessName())
                .businessEmail(supplier.getBusinessEmail())
                .category(supplier.getCategory())
                .city(supplier.getCity())
                .state(supplier.getState())
                .status(supplier.getStatus() != null ? supplier.getStatus().name() : "APPROVED")
                .build();

        return SupplierPublicCatalogDTO.builder()
                .supplier(supplierSummary)
                .products(dtoPage)
                .totalProducts(productsPage.getTotalElements())
                .build();
    }
}
