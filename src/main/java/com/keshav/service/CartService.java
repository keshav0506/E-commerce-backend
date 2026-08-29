package com.keshav.service;

import com.keshav.dto.CartItemRequestDTO;
import com.keshav.dto.CartItemResponseDTO;
import com.keshav.dto.CartResponseDTO;
import com.keshav.entity.Cart;
import com.keshav.entity.CartItem;
import com.keshav.entity.Product;
import com.keshav.entity.User;
import com.keshav.exception.CartItemNotFoundException;
import com.keshav.exception.CartNotFoundException;
import com.keshav.exception.ProductNotFoundException;
import com.keshav.repository.CartItemRepository;
import com.keshav.repository.CartRepository;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
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

    private Cart getOrCreateCart(String guestSessionId) {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setUser(user);
                        return cartRepository.save(c);
                    });
        }

        String validGuestId = (guestSessionId != null && !guestSessionId.isBlank())
                ? guestSessionId.trim()
                : "guest_default";

        return cartRepository.findByGuestSessionId(validGuestId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setGuestSessionId(validGuestId);
                    return cartRepository.save(c);
                });
    }

    @Override
    public CartResponseDTO getMyCart(String guestSessionId) {
        Cart cart = getOrCreateCart(guestSessionId);
        return convertToResponseDTO(cart);
    }

    @Override
    public CartResponseDTO addToCart(
            CartItemRequestDTO request,
            String guestSessionId) {

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + request.getProductId()
                        )
                );

        Cart cart = getOrCreateCart(guestSessionId);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(cartItem);
        return convertToResponseDTO(cart);
    }

    @Override
    public CartResponseDTO updateCartItem(
            Long cartItemId,
            int quantity,
            String guestSessionId) {

        Cart cart = getOrCreateCart(guestSessionId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException("Cart item not found with id: " + cartItemId)
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new CartItemNotFoundException("Cart item not found in your cart");
        }

        if (quantity < 1) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return convertToResponseDTO(cart);
    }

    @Override
    public void removeCartItem(Long cartItemId, String guestSessionId) {
        Cart cart = getOrCreateCart(guestSessionId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new CartItemNotFoundException("Cart item not found with id: " + cartItemId)
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(String guestSessionId) {
        Cart cart = getOrCreateCart(guestSessionId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public void mergeGuestCart(User user, String guestSessionId) {
        if (guestSessionId == null || guestSessionId.isBlank()) return;

        Optional<Cart> guestCartOpt = cartRepository.findByGuestSessionId(guestSessionId.trim());
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) return;

        Cart guestCart = guestCartOpt.get();
        Cart userCart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        for (CartItem gItem : guestCart.getItems()) {
            Optional<CartItem> existingItemOpt = cartItemRepository
                    .findByCartIdAndProductId(userCart.getId(), gItem.getProduct().getId());

            if (existingItemOpt.isPresent()) {
                CartItem existing = existingItemOpt.get();
                existing.setQuantity(existing.getQuantity() + gItem.getQuantity());
                cartItemRepository.save(existing);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(userCart);
                newItem.setProduct(gItem.getProduct());
                newItem.setQuantity(gItem.getQuantity());
                cartItemRepository.save(newItem);
            }
        }

        cartRepository.delete(guestCart);
    }

    private CartResponseDTO convertToResponseDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems()
                .stream()
                .map(this::convertItemToDTO)
                .toList();

        double totalAmount = items.stream()
                .mapToDouble(CartItemResponseDTO::getTotalPrice)
                .sum();

        return new CartResponseDTO(
                cart.getId(),
                items,
                totalAmount
        );
    }

    private CartItemResponseDTO convertItemToDTO(CartItem item) {
        Product product = item.getProduct();
        double totalPrice = product.getPrice() * item.getQuantity();

        return new CartItemResponseDTO(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImage(),
                item.getQuantity(),
                totalPrice
        );
    }
}