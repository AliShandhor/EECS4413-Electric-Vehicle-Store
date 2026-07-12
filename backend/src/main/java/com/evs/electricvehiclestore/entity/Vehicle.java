package com.evs.electricvehiclestore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private int modelYear;
    private double price;
    private int mileage;
    private String shape;
    private boolean hotDeal;

    @Column(nullable = false)
    private boolean available = true;

    public Vehicle() {
    }

    public Vehicle(
            String brand,
            String model,
            int modelYear,
            double price,
            int mileage,
            String shape,
            boolean hotDeal
    ) {
        this.brand = brand;
        this.model = model;
        this.modelYear = modelYear;
        this.price = price;
        this.mileage = mileage;
        this.shape = shape;
        this.hotDeal = hotDeal;
        this.available = true;
    }

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getModelYear() {
        return modelYear;
    }

    public double getPrice() {
        return price;
    }

    public int getMileage() {
        return mileage;
    }

    public String getShape() {
        return shape;
    }

    public boolean isHotDeal() {
        return hotDeal;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setModelYear(int modelYear) {
        this.modelYear = modelYear;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setHotDeal(boolean hotDeal) {
        this.hotDeal = hotDeal;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}