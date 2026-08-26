package com.keshav.repository;

import com.keshav.entity.Order;
import com.keshav.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);
}