package com.keshav.service;

import com.keshav.dto.*;
import com.keshav.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPurchaseOrderService {

    Page<PurchaseOrderDTO> getSupplierPurchaseOrders(PurchaseOrderStatus status, String search, Pageable pageable);

    PurchaseOrderDTO getSupplierPurchaseOrderById(Long id);

    PurchaseOrderDTO acceptPurchaseOrder(Long id, PurchaseOrderActionDTO request);

    PurchaseOrderDTO rejectPurchaseOrder(Long id, PurchaseOrderActionDTO request);

    PurchaseOrderDTO processPurchaseOrder(Long id, PurchaseOrderActionDTO request);

    PurchaseOrderDTO shipPurchaseOrder(Long id, PurchaseOrderActionDTO request);

    PurchaseOrderDTO deliverPurchaseOrder(Long id, PurchaseOrderActionDTO request);

    PurchaseOrderDTO createPurchaseOrder(AdminCreatePurchaseOrderDTO request, String adminEmail);

    Page<PurchaseOrderDTO> getAllPurchaseOrdersAdmin(PurchaseOrderStatus status, Long supplierId, Pageable pageable);

    PurchaseOrderDTO getPurchaseOrderByIdAdmin(Long id);
}
