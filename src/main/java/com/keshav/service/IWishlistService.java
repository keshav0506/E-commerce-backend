package com.keshav.service;

import com.keshav.dto.WishlistResponseDTO;

public interface IWishlistService {

    WishlistResponseDTO getMyWishlist();

    WishlistResponseDTO addToWishlist(Long productId);

    WishlistResponseDTO removeFromWishlist(Long productId);

    WishlistResponseDTO toggleWishlist(Long productId);

    void clearWishlist();
}
