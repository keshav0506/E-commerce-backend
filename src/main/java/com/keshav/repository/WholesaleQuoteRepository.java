package com.keshav.repository;

import com.keshav.entity.WholesaleQuoteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WholesaleQuoteRepository extends JpaRepository<WholesaleQuoteRequest, Long> {
    List<WholesaleQuoteRequest> findBySupplierId(Long supplierId);
    long countBySupplierId(Long supplierId);
}
