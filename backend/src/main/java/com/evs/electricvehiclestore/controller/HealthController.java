package com.evs.electricvehiclestore.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(image_available) FROM vehicle",
                    Long.class
            );
            return ResponseEntity.ok(status("UP"));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(status("DOWN"));
        }
    }

    private Map<String, Object> status(String status) {
        return Map.of(
            "status", status,
            "service", "electric-vehicle-store",
            "database", "UP".equals(status) ? "UP" : "DOWN",
            "timestamp", Instant.now().toString()
        );
    }
}
