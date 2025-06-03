package com.greenbasket.api.controller;

import com.greenbasket.api.model.Cart;
import com.greenbasket.api.model.User;
import com.greenbasket.api.repository.UserRepository;
import com.greenbasket.api.security.services.UserDetailsImpl;
import com.greenbasket.api.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCart() {
        User user = getCurrentUser();
        Cart cart = cartService.getOrCreateCart(user);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addItemToCart(@RequestBody Map<String, Object> payload) {
        User user = getCurrentUser();
        Long productId = Long.parseLong(payload.get("productId").toString());
        int quantity = Integer.parseInt(payload.get("quantity").toString());

        Cart cart = cartService.addItemToCart(user, productId, quantity);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @PatchMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> payload) {
        User user = getCurrentUser();
        int quantity = payload.get("quantity");

        Cart cart = cartService.updateItemQuantity(user, productId, quantity);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long productId) {
        User user = getCurrentUser();
        Cart cart = cartService.removeItemFromCart(user, productId);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> clearCart() {
        User user = getCurrentUser();
        cartService.clearCart(user);
        return ResponseEntity.ok().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email;
        
        if (principal instanceof UserDetailsImpl) {
            email = ((UserDetailsImpl) principal).getEmail();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            throw new RuntimeException("Invalid authentication principal type");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // DTO for Cart response
    private static class CartResponse {
        private final Long id;
        private final java.util.List<CartItemResponse> items;

        public CartResponse(Cart cart) {
            this.id = cart.getId();
            this.items = cart.getItems().stream()
                    .map(CartItemResponse::new)
                    .collect(Collectors.toList());
        }

        public Long getId() {
            return id;
        }

        public java.util.List<CartItemResponse> getItems() {
            return items;
        }
    }

    // DTO for CartItem response
    private static class CartItemResponse {
        private final Long id;
        private final Long productId;
        private final String productName;
        private final BigDecimal price;
        private final int quantity;
        private final String imageUrl;

        public CartItemResponse(com.greenbasket.api.model.CartItem item) {
            this.id = item.getId();
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
            this.price = item.getProduct().getPrice();
            this.quantity = item.getQuantity();
            this.imageUrl = item.getProduct().getImageUrl();
        }

        public Long getId() {
            return id;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
