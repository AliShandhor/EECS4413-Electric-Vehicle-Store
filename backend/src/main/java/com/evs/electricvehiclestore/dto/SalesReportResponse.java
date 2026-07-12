package com.evs.electricvehiclestore.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record SalesReportResponse(
        Instant generatedAt,
        int totalOrders,
        int completedSales,
        int excludedOrders,
        int totalVehiclesSold,
        BigDecimal grossRevenue,
        BigDecimal averageOrderValue,
        Map<String, Long> ordersByStatus,
        List<VehicleSalesView> vehicleSales,
        VehicleSalesView topSellingVehicle,
        String message
) {

    public record VehicleSalesView(
            Long vehicleId,
            String brand,
            String model,
            int unitsSold,
            BigDecimal revenue
    ) {
    }
}