package com.evs.electricvehiclestore.dto;

import java.util.List;

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
    private List<AccessoryDTO> accessories = List.of();

    public OrderItemDTO() {
    }

    public OrderItemDTO(Long vehicleId, String brand, String model, int quantity, double unitPrice) {
        this(vehicleId, brand, model, quantity, unitPrice, List.of());
    }

    public OrderItemDTO(Long vehicleId, String brand, String model, int quantity, double unitPrice,
                        List<AccessoryDTO> accessories) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.model = model;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice * quantity;
        this.accessories = accessories == null ? List.of() : accessories;
    }

    public Long getVehicleId() { return vehicleId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return lineTotal; }
    public List<AccessoryDTO> getAccessories() { return accessories; }

    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    public void setAccessories(List<AccessoryDTO> accessories) { this.accessories = accessories; }
}
