package com.evs.electricvehiclestore.dto;

/**
 * @author Uzma Alam
 */
public class PaymentResultDTO {

    private Long orderId;
    private boolean approved;
    private String message;
    private String maskedCardNumber;

    public PaymentResultDTO() {
    }

    public PaymentResultDTO(Long orderId, boolean approved, String message, String maskedCardNumber) {
        this.orderId = orderId;
        this.approved = approved;
        this.message = message;
        this.maskedCardNumber = maskedCardNumber;
    }

    public Long getOrderId() { return orderId; }
    public boolean isApproved() { return approved; }
    public String getMessage() { return message; }
    public String getMaskedCardNumber() { return maskedCardNumber; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setMessage(String message) { this.message = message; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
}