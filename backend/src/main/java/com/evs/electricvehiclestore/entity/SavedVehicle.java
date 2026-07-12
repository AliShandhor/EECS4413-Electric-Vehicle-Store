package com.evs.electricvehiclestore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "saved_vehicle",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_vehicle_user_vehicle",
                columnNames = {"user_id", "vehicle_id"}
        )
)
public class SavedVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    public SavedVehicle() {
    }

    public SavedVehicle(Long userId, Long vehicleId) {
        this.userId = userId;
        this.vehicleId = vehicleId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}