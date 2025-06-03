package com.greenbasket.api.repository;

import com.greenbasket.api.model.Order;
import com.greenbasket.api.model.OrderStatus;
import com.greenbasket.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.product.seller.id = :sellerId")
    Page<Order> findOrdersForSeller(Long sellerId, Pageable pageable);

    List<Order> findByUser(User user);
}
