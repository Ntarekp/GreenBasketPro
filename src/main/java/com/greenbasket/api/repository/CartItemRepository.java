package com.greenbasket.api.repository;

import com.greenbasket.api.model.Cart;
import com.greenbasket.api.model.CartItem;
import com.greenbasket.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    void deleteByCartIdAndProductId(Long cartId, Long productId);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
