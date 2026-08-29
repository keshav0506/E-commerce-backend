package com.keshav.repository;

import com.keshav.entity.SupplierProfile;
import com.keshav.entity.SupplierStatus;
import com.keshav.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierProfileRepository extends JpaRepository<SupplierProfile, Long> {

    Optional<SupplierProfile> findByUser(User user);

    Optional<SupplierProfile> findByUserId(Long userId);

    Optional<SupplierProfile> findByBusinessEmail(String businessEmail);

    boolean existsByBusinessEmail(String businessEmail);

    boolean existsByTaxIdentifier(String taxIdentifier);

    List<SupplierProfile> findByStatus(SupplierStatus status);

    Page<SupplierProfile> findByStatus(SupplierStatus status, Pageable pageable);

    long countByStatus(SupplierStatus status);
}
