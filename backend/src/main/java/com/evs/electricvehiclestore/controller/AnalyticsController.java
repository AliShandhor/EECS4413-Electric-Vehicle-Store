package com.evs.electricvehiclestore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evs.electricvehiclestore.dto.SalesReportResponse;
import com.evs.electricvehiclestore.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales")
    public SalesReportResponse vehicleSalesReport() {
        return analyticsService.vehicleSalesReport();
    }

    @GetMapping("/usage")
    public String usageReport() {
        return analyticsService.usageReport();
    }
}