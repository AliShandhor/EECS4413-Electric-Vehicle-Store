package com.evs.electricvehiclestore.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByOrderIdIn(
            Collection<Long> orderIds
    );
}