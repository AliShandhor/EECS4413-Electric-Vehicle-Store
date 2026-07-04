package com.evs.electricvehiclestore.entity;

import jakarta.persistence.*;

@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vehicleId;
    private Long userId;
    private int rating;
    private String comment;

    public Review() {
    }

    public Review(Long vehicleId, Long userId, int rating, String comment) {
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public Long getVehicleId() { return vehicleId; }
    public Long getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }

    public void setId(Long id) { this.id = id; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}