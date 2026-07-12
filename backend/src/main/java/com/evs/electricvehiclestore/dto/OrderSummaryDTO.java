package com.evs.electricvehiclestore.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Uzma Alam
 */
public class OrderSummaryDTO {

    private Long orderId;
    private Long userId;
    private String status;
    private double totalAmount;
    private LocalDateTime orderDate;
    private ShippingInfoDTO shippingInfo;
    private List<OrderItemDTO> items;

    public OrderSummaryDTO() {
    }

    public OrderSummaryDTO(Long orderId, Long userId, String status, double totalAmount,
                            LocalDateTime orderDate, ShippingInfoDTO shippingInfo, List<OrderItemDTO> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.shippingInfo = shippingInfo;
        this.items = items;
    }

    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public ShippingInfoDTO getShippingInfo() { return shippingInfo; }
    public List<OrderItemDTO> getItems() { return items; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public void setShippingInfo(ShippingInfoDTO shippingInfo) { this.shippingInfo = shippingInfo; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}