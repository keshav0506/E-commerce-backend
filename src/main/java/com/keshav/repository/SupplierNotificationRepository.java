package com.keshav.repository;

import com.keshav.entity.SupplierNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierNotificationRepository extends JpaRepository<SupplierNotification, Long> {

    List<SupplierNotification> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);

    Page<SupplierNotification> findBySupplierIdOrderByCreatedAtDesc(Long supplierId, Pageable pageable);

    long countBySupplierIdAndIsReadFalse(Long supplierId);
}
