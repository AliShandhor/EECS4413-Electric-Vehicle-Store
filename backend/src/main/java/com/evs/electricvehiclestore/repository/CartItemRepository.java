package com.evs.electricvehiclestore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.CartItem;

/**
 * @author Uzma Alam
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);
}