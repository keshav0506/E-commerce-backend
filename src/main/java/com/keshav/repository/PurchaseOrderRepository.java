package com.keshav.repository;

import com.keshav.entity.PurchaseOrder;
import com.keshav.entity.PurchaseOrderStatus;
import com.keshav.entity.SupplierProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    Optional<PurchaseOrder> findByIdAndSupplier(Long id, SupplierProfile supplier);

    Page<PurchaseOrder> findBySupplierOrderByCreatedAtDesc(SupplierProfile supplier, Pageable pageable);

    Page<PurchaseOrder> findBySupplierAndStatusOrderByCreatedAtDesc(SupplierProfile supplier, PurchaseOrderStatus status, Pageable pageable);

    List<PurchaseOrder> findBySupplierOrderByCreatedAtDesc(SupplierProfile supplier);

    long countBySupplierAndStatus(SupplierProfile supplier, PurchaseOrderStatus status);

    long countBySupplier(SupplierProfile supplier);

    Page<PurchaseOrder> findByStatusOrderByCreatedAtDesc(PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.supplier = :supplier AND po.status IN :statuses")
    long countBySupplierAndStatuses(@Param("supplier") SupplierProfile supplier, @Param("statuses") List<PurchaseOrderStatus> statuses);
}
