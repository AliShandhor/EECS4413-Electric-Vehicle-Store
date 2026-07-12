package com.evs.electricvehiclestore.repository;

import com.evs.electricvehiclestore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Preserved for compatibility with Uzma's checkout implementation.
    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndVehicleId(
            Long cartId,
            Long vehicleId
    );

    boolean existsByCartIdAndVehicleId(
            Long cartId,
            Long vehicleId
    );
}