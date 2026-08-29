package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDashboardDTO {

    private long pendingOrders;
    private long acceptedOrders;
    private long ordersToShip;
    private long inTransit;
    private long completedSupplies;
    private long rejectedOrders;
    private long totalPurchaseOrders;
    private BigDecimal totalRevenue;
    private double onTimeDeliveryRate;
    private double fulfillmentRate;
    private long totalProductsListed;
    private long lowStockProductsCount;
    private List<PurchaseOrderDTO> recentPurchaseOrders;
    private List<SupplierNotificationDTO> recentNotifications;
}
