package com.keshav.service;

import com.keshav.dto.WishlistItemResponseDTO;
import com.keshav.dto.WishlistResponseDTO;
import com.keshav.entity.Product;
import com.keshav.entity.User;
import com.keshav.entity.WishlistItem;
import com.keshav.exception.ProductNotFoundException;
import com.keshav.exception.UserNotFoundException;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.UserRepository;
import com.keshav.repository.WishlistItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User is not authenticated");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponseDTO getMyWishlist() {
        User user = getCurrentUser();
        List<WishlistItem> items = wishlistRepository.findByUserOrderByCreatedAtDesc(user);
        return buildWishlistResponse(items);
    }

    @Override
    public WishlistResponseDTO addToWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (!wishlistRepository.existsByUserAndProduct(user, product)) {
            WishlistItem item = new WishlistItem(user, product);
            wishlistRepository.save(item);
        }

        return getMyWishlist();
    }

    @Override
    public WishlistResponseDTO removeFromWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        wishlistRepository.findByUserAndProduct(user, product)
                .ifPresent(wishlistRepository::delete);

        return getMyWishlist();
    }

    @Override
    public WishlistResponseDTO toggleWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Optional<WishlistItem> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
        } else {
            WishlistItem item = new WishlistItem(user, product);
            wishlistRepository.save(item);
        }

        return getMyWishlist();
    }

    @Override
    public void clearWishlist() {
        User user = getCurrentUser();
        wishlistRepository.deleteByUser(user);
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
