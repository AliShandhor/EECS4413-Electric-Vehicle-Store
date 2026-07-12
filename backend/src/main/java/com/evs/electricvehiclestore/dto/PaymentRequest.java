package com.evs.electricvehiclestore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @author Uzma Alam
 */
public class PaymentRequest {

    @NotNull(message = "Credit card information is required")
    @Valid
    private CreditCardDTO creditCard;

    public PaymentRequest() {
    }

    public CreditCardDTO getCreditCard() { return creditCard; }
    public void setCreditCard(CreditCardDTO creditCard) { this.creditCard = creditCard; }
}