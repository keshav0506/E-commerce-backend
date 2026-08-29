package com.keshav.repository;

import com.keshav.entity.Product;
import com.keshav.entity.User;
import com.keshav.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserOrderByCreatedAtDesc(User user);

    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<WishlistItem> findByGuestSessionIdOrderByCreatedAtDesc(String guestSessionId);

    Optional<WishlistItem> findByUserAndProduct(User user, Product product);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    Optional<WishlistItem> findByGuestSessionIdAndProduct(String guestSessionId, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    boolean existsByGuestSessionIdAndProduct(String guestSessionId, Product product);

    void deleteByUserAndProduct(User user, Product product);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    void deleteByGuestSessionIdAndProduct(String guestSessionId, Product product);

    void deleteByUser(User user);

    void deleteByGuestSessionId(String guestSessionId);
}
