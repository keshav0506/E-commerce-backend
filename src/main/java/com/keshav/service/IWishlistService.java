package com.keshav.service;

import com.keshav.dto.WishlistResponseDTO;
import com.keshav.entity.User;

public interface IWishlistService {

    WishlistResponseDTO getMyWishlist(String guestSessionId);

    WishlistResponseDTO addToWishlist(Long productId, String guestSessionId);

    WishlistResponseDTO removeFromWishlist(Long productId, String guestSessionId);

    WishlistResponseDTO toggleWishlist(Long productId, String guestSessionId);

    void clearWishlist(String guestSessionId);

    void mergeGuestWishlist(User user, String guestSessionId);
}

