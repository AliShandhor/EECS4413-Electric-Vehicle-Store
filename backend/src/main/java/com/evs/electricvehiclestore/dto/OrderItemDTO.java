package com.evs.electricvehiclestore.dto;

/**
 * @author Uzma Alam
 */
public class OrderItemDTO {

    private Long vehicleId;
    private String brand;
    private String model;
    private int quantity;
    private double unitPrice;
    private double lineTotal;

    public OrderItemDTO() {
    }

    public OrderItemDTO(Long vehicleId, String brand, String model, int quantity, double unitPrice) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.model = model;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice * quantity;
    }

    public Long getVehicleId() { return vehicleId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return lineTotal; }

    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
}