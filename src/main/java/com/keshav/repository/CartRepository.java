package com.keshav.repository;

import com.keshav.entity.Cart;
import com.keshav.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByGuestSessionId(String guestSessionId);

    void deleteByGuestSessionId(String guestSessionId);
}