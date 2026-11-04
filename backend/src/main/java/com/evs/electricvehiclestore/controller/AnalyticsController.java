package com.evs.electricvehiclestore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evs.electricvehiclestore.dto.SalesReportResponse;
import com.evs.electricvehiclestore.dto.UsageReportResponse;
import com.evs.electricvehiclestore.service.AnalyticsService;
import com.evs.electricvehiclestore.service.UsageTrackingService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UsageTrackingService usageTrackingService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            UsageTrackingService usageTrackingService
    ) {
        this.analyticsService = analyticsService;
        this.usageTrackingService = usageTrackingService;
    }

    @GetMapping("/sales")
    public SalesReportResponse vehicleSalesReport() {
        return analyticsService.vehicleSalesReport();
    }

    @GetMapping("/usage")
    public UsageReportResponse usageReport() {
        return usageTrackingService.report();
    }
}
