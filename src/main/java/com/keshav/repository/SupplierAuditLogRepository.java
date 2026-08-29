package com.keshav.repository;

import com.keshav.entity.SupplierAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierAuditLogRepository extends JpaRepository<SupplierAuditLog, Long> {

    Page<SupplierAuditLog> findBySupplierIdOrderByCreatedAtDesc(Long supplierId, Pageable pageable);

    List<SupplierAuditLog> findTop20BySupplierIdOrderByCreatedAtDesc(Long supplierId);
}
