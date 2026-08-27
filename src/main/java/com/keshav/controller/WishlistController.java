package com.keshav.controller;

import com.keshav.dto.WishlistResponseDTO;
import com.keshav.service.IWishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final IWishlistService wishlistService;

    public WishlistController(IWishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistResponseDTO> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getMyWishlist());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> addToWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(productId));
    }

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<WishlistResponseDTO> toggleWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.toggleWishlist(productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> removeFromWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        wishlistService.clearWishlist();
        return ResponseEntity.noContent().build();
    }
}
