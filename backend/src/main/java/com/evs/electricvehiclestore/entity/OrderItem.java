package com.evs.electricvehiclestore.entity;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long vehicleId;
    private int quantity;
    private double price;

    public OrderItem() {
    }

    public OrderItem(Long orderId, Long vehicleId, int quantity, double price) {
        this.orderId = orderId;
        this.vehicleId = vehicleId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getVehicleId() { return vehicleId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void setId(Long id) { this.id = id; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
}