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
    public ResponseEntity<WishlistResponseDTO> getMyWishlist(
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {
        return ResponseEntity.ok(wishlistService.getMyWishlist(guestSessionId));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> addToWishlist(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(productId, guestSessionId));
    }

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<WishlistResponseDTO> toggleWishlist(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {
        return ResponseEntity.ok(wishlistService.toggleWishlist(productId, guestSessionId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistResponseDTO> removeFromWishlist(
            @PathVariable Long productId,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(productId, guestSessionId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearWishlist(
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {
        wishlistService.clearWishlist(guestSessionId);
        return ResponseEntity.noContent().build();
    }
}

