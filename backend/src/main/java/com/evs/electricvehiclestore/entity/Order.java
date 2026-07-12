package com.evs.electricvehiclestore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private double totalAmount;
    private String status;
    private LocalDateTime orderDate;

    // Shipping information (UC9)
    private String shippingStreet;
    private String shippingCity;
    private String shippingProvince;
    private String shippingCountry;
    private String shippingZip;
    private String shippingPhone;

    public Order() {
    }

    public Order(Long userId, double totalAmount, String status) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getShippingStreet() { return shippingStreet; }
    public String getShippingCity() { return shippingCity; }
    public String getShippingProvince() { return shippingProvince; }
    public String getShippingCountry() { return shippingCountry; }
    public String getShippingZip() { return shippingZip; }
    public String getShippingPhone() { return shippingPhone; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setShippingStreet(String shippingStreet) { this.shippingStreet = shippingStreet; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public void setShippingProvince(String shippingProvince) { this.shippingProvince = shippingProvince; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
    public void setShippingZip(String shippingZip) { this.shippingZip = shippingZip; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
}