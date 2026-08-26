package com.keshav.service;

import com.keshav.dto.CartItemRequestDTO;
import com.keshav.dto.CartResponseDTO;

public interface ICartService {

    CartResponseDTO getMyCart();

    CartResponseDTO addToCart(
            CartItemRequestDTO request);

    CartResponseDTO updateCartItem(
            Long cartItemId,
            int quantity);

    void removeCartItem(Long cartItemId);

    void clearCart();
}