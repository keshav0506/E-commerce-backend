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

import java.util.List;

@Service
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

    @Override
    public CartResponseDTO getMyCart() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        return convertToResponseDTO(cart);
    }

    @Override
    public CartResponseDTO addToCart(
            CartItemRequestDTO request) {

        User user = getCurrentUser();

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        )
                );

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (cartItem != null) {

            cartItem.setQuantity(
                    cartItem.getQuantity()
                            + request.getQuantity()
            );

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
            int quantity) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for current user"
                        )
                );

        CartItem cartItem =
                cartItemRepository.findById(cartItemId)
                        .orElseThrow(() ->
                                new CartItemNotFoundException(
                                        "Cart item not found with id: "
                                                + cartItemId
                                )
                        );

        if (!cartItem.getCart().getId()
                .equals(cart.getId())) {

            throw new CartItemNotFoundException(
                    "Cart item not found in your cart"
            );
        }

        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return convertToResponseDTO(cart);
    }

    @Override
    public void removeCartItem(Long cartItemId) {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for current user"
                        )
                );

        CartItem cartItem =
                cartItemRepository.findById(cartItemId)
                        .orElseThrow(() ->
                                new CartItemNotFoundException(
                                        "Cart item not found with id: "
                                                + cartItemId
                                )
                        );

        if (!cartItem.getCart().getId()
                .equals(cart.getId())) {

            throw new RuntimeException(
                    "Cart item does not belong to your cart"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart() {

        User user = getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private Cart createCart(User user) {

        Cart cart = new Cart();

        cart.setUser(user);

        return cartRepository.save(cart);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }

    private CartResponseDTO convertToResponseDTO(
            Cart cart) {

        List<CartItemResponseDTO> items =
                cart.getItems()
                        .stream()
                        .map(this::convertItemToDTO)
                        .toList();

        double totalAmount =
                items.stream()
                        .mapToDouble(
                                CartItemResponseDTO::getTotalPrice
                        )
                        .sum();

        return new CartResponseDTO(
                cart.getId(),
                items,
                totalAmount
        );
    }

    private CartItemResponseDTO convertItemToDTO(
            CartItem item) {

        Product product = item.getProduct();

        double totalPrice =
                product.getPrice() * item.getQuantity();

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