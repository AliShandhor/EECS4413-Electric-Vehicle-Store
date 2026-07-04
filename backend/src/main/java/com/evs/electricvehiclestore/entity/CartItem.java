package com.evs.electricvehiclestore.entity;

import jakarta.persistence.*;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cartId;
    private Long vehicleId;
    private int quantity;

    public CartItem() {
    }

    public CartItem(Long cartId, Long vehicleId, int quantity) {
        this.cartId = cartId;
        this.vehicleId = vehicleId;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getCartId() { return cartId; }
    public Long getVehicleId() { return vehicleId; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}