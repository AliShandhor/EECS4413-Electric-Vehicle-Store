package com.evs.electricvehiclestore.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_image")
public class VehicleImage {

    @Id
    private Long vehicleId;

    @Column(nullable = false, length = 100)
    private String contentType;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(nullable = false, length = 5_242_880)
    private byte[] data;

    public VehicleImage() {
    }

    public VehicleImage(Long vehicleId, String contentType, byte[] data) {
        this.vehicleId = vehicleId;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
