package com.evs.electricvehiclestore.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

/**
 * @author Uzma Alam
 */
@Service
public class PaymentService {

    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    /**
     * @return true if the payment is approved, false if denied.
     */
    public boolean processPayment() {
        int attempt = attemptCounter.incrementAndGet();
        return attempt % 3 != 0;
    }

    public void resetForTesting() {
        attemptCounter.set(0);
    }
}