package com.evs.electricvehiclestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @author Uzma Alam
 */
public class CheckoutRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "Shipping information is required")
    @Valid
    private ShippingInfoDTO shippingInfo;

    public CheckoutRequest() {
    }

    public Long getUserId() { return userId; }
    public ShippingInfoDTO getShippingInfo() { return shippingInfo; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setShippingInfo(ShippingInfoDTO shippingInfo) { this.shippingInfo = shippingInfo; }
}