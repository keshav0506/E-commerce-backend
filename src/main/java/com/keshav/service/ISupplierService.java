package com.keshav.service;

import com.keshav.dto.*;
import com.keshav.entity.SupplierProfile;
import com.keshav.entity.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ISupplierService {

    SupplierProfileDTO applySupplier(SupplierApplyRequestDTO request);

    SupplierProfileDTO getMyProfile();

    SupplierProfileDTO updateMyProfile(SupplierProfileDTO request);

    SupplierDashboardDTO getDashboardMetrics();

    Page<SupplierProfileDTO> getAllSuppliers(SupplierStatus status, Pageable pageable);

    SupplierProfileDTO getSupplierById(Long id);

    SupplierProfileDTO updateSupplierStatus(Long id, SupplierStatusUpdateDTO request, String adminEmail);

    SupplierProfile getAuthenticatedSupplier();

    List<SupplierNotificationDTO> getMyNotifications();

    void markNotificationAsRead(Long notificationId);

    Page<SupplierProductDTO> getMyProducts(String search, Pageable pageable);

    SupplierProductDTO createProduct(SupplierProductRequestDTO request);

    SupplierProductDTO getMyProductById(Long productId);

    SupplierProductDTO updateProduct(Long productId, SupplierProductRequestDTO request);

    void deleteProduct(Long productId);

    SupplierProductDTO updateProductStock(Long productId, int stock);

    SupplierPublicCatalogDTO getPublicSupplierCatalog(Long supplierId, String search, Pageable pageable);
}
