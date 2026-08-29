package com.keshav.service;

import com.keshav.dto.WishlistItemResponseDTO;
import com.keshav.dto.WishlistResponseDTO;
import com.keshav.entity.Product;
import com.keshav.entity.User;
import com.keshav.entity.WishlistItem;
import com.keshav.exception.ProductNotFoundException;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.UserRepository;
import com.keshav.repository.WishlistItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistService implements IWishlistService {

    private final WishlistItemRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistItemRepository wishlistRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponseDTO getMyWishlist(String guestSessionId) {
        Optional<User> userOpt = getAuthenticatedUser();
        List<WishlistItem> items;
        if (userOpt.isPresent()) {
            items = wishlistRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
        } else {
            String validGuestId = (guestSessionId != null && !guestSessionId.isBlank()) ? guestSessionId.trim() : "guest_default";
            items = wishlistRepository.findByGuestSessionIdOrderByCreatedAtDesc(validGuestId);
        }
        return buildWishlistResponse(items);
    }

    @Override
    public WishlistResponseDTO addToWishlist(Long productId, String guestSessionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!wishlistRepository.existsByUserAndProduct(user, product)) {
                WishlistItem item = new WishlistItem(user, product);
                wishlistRepository.save(item);
            }
        } else {
            String validGuestId = (guestSessionId != null && !guestSessionId.isBlank()) ? guestSessionId.trim() : "guest_default";
            if (!wishlistRepository.existsByGuestSessionIdAndProduct(validGuestId, product)) {
                WishlistItem item = new WishlistItem(validGuestId, product);
                wishlistRepository.save(item);
            }
        }

        return getMyWishlist(guestSessionId);
    }

    @Override
    public WishlistResponseDTO removeFromWishlist(Long productId, String guestSessionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isPresent()) {
            wishlistRepository.findByUserAndProduct(userOpt.get(), product)
                    .ifPresent(wishlistRepository::delete);
        } else {
            String validGuestId = (guestSessionId != null && !guestSessionId.isBlank()) ? guestSessionId.trim() : "guest_default";
            wishlistRepository.findByGuestSessionIdAndProduct(validGuestId, product)
                    .ifPresent(wishlistRepository::delete);
        }

        return getMyWishlist(guestSessionId);
    }

    @Override
    public WishlistResponseDTO toggleWishlist(Long productId, String guestSessionId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<WishlistItem> existing = wishlistRepository.findByUserAndProduct(user, product);
            if (existing.isPresent()) {
                wishlistRepository.delete(existing.get());
            } else {
                WishlistItem item = new WishlistItem(user, product);
                wishlistRepository.save(item);
            }
        } else {
            String validGuestId = (guestSessionId != null && !guestSessionId.isBlank()) ? guestSessionId.trim() : "guest_default";
            Optional<WishlistItem> existing = wishlistRepository.findByGuestSessionIdAndProduct(validGuestId, product);
            if (existing.isPresent()) {
                wishlistRepository.delete(existing.get());
            } else {
                WishlistItem item = new WishlistItem(validGuestId, product);
                wishlistRepository.save(item);
            }
        }

        return getMyWishlist(guestSessionId);
    }

    @Override
    public void clearWishlist(String guestSessionId) {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isPresent()) {
            wishlistRepository.deleteByUser(userOpt.get());
        } else {
            String validGuestId = (guestSessionId != null && !guestSessionId.isBlank()) ? guestSessionId.trim() : "guest_default";
            wishlistRepository.deleteByGuestSessionId(validGuestId);
        }
    }

    @Override
    public void mergeGuestWishlist(User user, String guestSessionId) {
        if (guestSessionId == null || guestSessionId.isBlank()) return;

        List<WishlistItem> guestItems = wishlistRepository.findByGuestSessionIdOrderByCreatedAtDesc(guestSessionId.trim());
        if (guestItems.isEmpty()) return;

        for (WishlistItem gItem : guestItems) {
            if (!wishlistRepository.existsByUserAndProduct(user, gItem.getProduct())) {
                WishlistItem userItem = new WishlistItem(user, gItem.getProduct());
                wishlistRepository.save(userItem);
            }
        }

        wishlistRepository.deleteByGuestSessionId(guestSessionId.trim());
    }

    private WishlistResponseDTO buildWishlistResponse(List<WishlistItem> items) {
        List<WishlistItemResponseDTO> itemDTOs = items.stream().map(item -> {
            Product p = item.getProduct();
            WishlistItemResponseDTO dto = new WishlistItemResponseDTO();
            dto.setId(item.getId());
            dto.setProductId(p.getId());
            dto.setProductName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setPrice(p.getPrice());
            dto.setStock(p.getStock());
            dto.setImage(p.getImage());
            dto.setStatus(p.getStatus());
            if (p.getCategory() != null) {
                dto.setCategoryId(p.getCategory().getId());
                dto.setCategoryName(p.getCategory().getName());
            }
            dto.setAddedAt(item.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        List<Long> productIds = items.stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        return new WishlistResponseDTO(itemDTOs, productIds, itemDTOs.size());
    }
}
