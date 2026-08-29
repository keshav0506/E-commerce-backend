package com.keshav.service;

import com.keshav.dto.CartItemRequestDTO;
import com.keshav.dto.CartResponseDTO;
import com.keshav.entity.User;

public interface ICartService {

    CartResponseDTO getMyCart(String guestSessionId);

    CartResponseDTO addToCart(
            CartItemRequestDTO request,
            String guestSessionId);

    CartResponseDTO updateCartItem(
            Long cartItemId,
            int quantity,
            String guestSessionId);

    void removeCartItem(Long cartItemId, String guestSessionId);

    void clearCart(String guestSessionId);

    void mergeGuestCart(User user, String guestSessionId);
}