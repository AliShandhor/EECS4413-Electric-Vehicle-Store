package com.evs.electricvehiclestore.entity;

import java.util.LinkedHashSet;
import java.util.Set;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_item_accessory", joinColumns = @JoinColumn(name = "order_item_id"))
    @Column(name = "accessory_id")
    private Set<Long> accessoryIds = new LinkedHashSet<>();

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
    public Set<Long> getAccessoryIds() { return accessoryIds; }

    public void setId(Long id) { this.id = id; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setAccessoryIds(Set<Long> accessoryIds) {
        this.accessoryIds = accessoryIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(accessoryIds);
    }
}
