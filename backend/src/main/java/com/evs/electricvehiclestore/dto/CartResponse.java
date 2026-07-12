package com.evs.electricvehiclestore.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        Long userId,
        int distinctItemCount,
        int totalQuantity,
        BigDecimal subtotal,
        List<CartItemView> items,
        int savedCount,
        List<SavedVehicleView> savedForLater,
        String message
) {

    public record CartItemView(
            Long cartItemId,
            Long vehicleId,
            String brand,
            String model,
            int modelYear,
            BigDecimal price,
            int mileage,
            String shape,
            boolean hotDeal,
            boolean available,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public record SavedVehicleView(
            Long savedVehicleId,
            Long vehicleId,
            String brand,
            String model,
            int modelYear,
            BigDecimal price,
            int mileage,
            String shape,
            boolean hotDeal,
            boolean available
    ) {
    }
}