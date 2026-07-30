package com.evs.electricvehiclestore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VehicleRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @Min(1900) int modelYear,
        @Positive double price,
        @Min(0) int mileage,
        @NotBlank String shape,
        boolean hotDeal,
        boolean available
) {
}
