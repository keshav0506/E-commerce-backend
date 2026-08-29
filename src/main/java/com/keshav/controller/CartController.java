package com.keshav.controller;

import com.keshav.dto.CartItemRequestDTO;
import com.keshav.dto.CartResponseDTO;
import com.keshav.service.ICartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart(
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {

        return ResponseEntity.ok(
                cartService.getMyCart(guestSessionId)
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            @Valid @RequestBody CartItemRequestDTO request,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(request, guestSessionId));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        cartItemId,
                        quantity,
                        guestSessionId
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId,
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {

        cartService.removeCartItem(cartItemId, guestSessionId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-Guest-Session-ID", required = false) String guestSessionId) {

        cartService.clearCart(guestSessionId);

        return ResponseEntity.noContent().build();
    }
}