package com.evs.electricvehiclestore.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String eventType;
    private String path;
    private Instant occurredAt;

    public UsageEvent() {
    }

    public UsageEvent(Long userId, String eventType, String path, Instant occurredAt) {
        this.userId = userId;
        this.eventType = eventType;
        this.path = path;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getPath() { return path; }
    public Instant getOccurredAt() { return occurredAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setPath(String path) { this.path = path; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
