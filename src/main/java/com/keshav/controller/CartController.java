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
    public ResponseEntity<CartResponseDTO> getMyCart() {

        return ResponseEntity.ok(
                cartService.getMyCart()
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addToCart(
            @Valid @RequestBody CartItemRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addToCart(request));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        cartItemId,
                        quantity
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId) {

        cartService.removeCartItem(cartItemId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {

        cartService.clearCart();

        return ResponseEntity.noContent().build();
    }
}