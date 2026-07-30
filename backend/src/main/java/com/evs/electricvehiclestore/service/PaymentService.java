package com.evs.electricvehiclestore.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evs.electricvehiclestore.dto.CreditCardDTO;

/**
 * @author Uzma Alam
 */
@Service
public class PaymentService {

    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    @Value("${payment.approval-mode:simulated}")
    private String approvalMode = "simulated";

    /**
     * @return true if the payment is approved, false if denied.
     */
    public boolean processPayment() {
        if ("always-approved".equalsIgnoreCase(approvalMode)) {
            return true;
        }
        int attempt = attemptCounter.incrementAndGet();
        return attempt % 3 != 0;
    }

    /**
     * Stable demo gateway: cards ending in 0000 are denied and all other
     * valid cards are approved. Local development retains the rotating
     * approved/denied simulator unless this mode is configured.
     */
    public boolean processPayment(CreditCardDTO creditCard) {
        if ("deterministic".equalsIgnoreCase(approvalMode)) {
            return creditCard != null
                    && creditCard.getCardNumber() != null
                    && !creditCard.getCardNumber().endsWith("0000");
        }
        return processPayment();
    }

    public void resetForTesting() {
        attemptCounter.set(0);
    }
}
