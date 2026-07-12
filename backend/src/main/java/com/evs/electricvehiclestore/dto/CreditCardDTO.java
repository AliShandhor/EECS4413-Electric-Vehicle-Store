package com.evs.electricvehiclestore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Carries credit card details from the client 
 * the card number must be typed in without spaces or dashes, e.g. 1234567890123456
 * @author Uzma Alam
 */
public class CreditCardDTO {

    @NotBlank(message = "Cardholder name is required")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    private String cardNumber;

    @NotBlank(message = "Expiry month is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Expiry month must be MM (01-12)")
    private String expiryMonth;

    @NotBlank(message = "Expiry year is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Expiry year must be YYYY")
    private String expiryYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3-4 digits")
    private String cvv;

    public CreditCardDTO() {
    }

    public String getCardHolderName() { return cardHolderName; }
    public String getCardNumber() { return cardNumber; }
    public String getExpiryMonth() { return expiryMonth; }
    public String getExpiryYear() { return expiryYear; }
    public String getCvv() { return cvv; }

    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public void setExpiryMonth(String expiryMonth) { this.expiryMonth = expiryMonth; }
    public void setExpiryYear(String expiryYear) { this.expiryYear = expiryYear; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    /** Only the last 4 digits, safe to echo back in a response or log if ever needed. */
    public String maskedNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}