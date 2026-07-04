package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales")
    public String vehicleSalesReport() {
        return analyticsService.vehicleSalesReport();
    }

    @GetMapping("/usage")
    public String usageReport() {
        return analyticsService.usageReport();
    }
}