package com.evs.electricvehiclestore.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.evs.electricvehiclestore.dto.CreditCardDTO;

/**
 * @author Uzma Alam
 
 */
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
      
        paymentService = new PaymentService();
    }

    @Test
    void firstTwoPaymentsAreApproved() {
        assertTrue(paymentService.processPayment(), "1st payment should be approved");
        assertTrue(paymentService.processPayment(), "2nd payment should be approved");
    }

    @Test
    void thirdPaymentIsDenied() {
        paymentService.processPayment(); 
        paymentService.processPayment(); 
        assertFalse(paymentService.processPayment(), "3rd payment should be denied");
    }

    @Test
    void patternRepeatsEveryThreeAttempts() {
        boolean[] results = new boolean[6];
        for (int i = 0; i < 6; i++) {
            results[i] = paymentService.processPayment();
        }
        // approved, approved, denied, approved, approved, denied
        assertTrue(results[0]);
        assertTrue(results[1]);
        assertFalse(results[2]);
        assertTrue(results[3]);
        assertTrue(results[4]);
        assertFalse(results[5]);
    }

    @Test
    void deterministicModeUsesCardSuffixForStableDemoResults() {
        ReflectionTestUtils.setField(paymentService, "approvalMode", "deterministic");

        CreditCardDTO approvedCard = new CreditCardDTO();
        approvedCard.setCardNumber("4242424242424242");
        CreditCardDTO deniedCard = new CreditCardDTO();
        deniedCard.setCardNumber("4242424242420000");

        assertTrue(paymentService.processPayment(approvedCard));
        assertFalse(paymentService.processPayment(deniedCard));
    }
}
