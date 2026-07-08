package com.evs.electricvehiclestore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.dto.CheckoutRequest;
import com.evs.electricvehiclestore.dto.CreditCardDTO;
import com.evs.electricvehiclestore.dto.OrderSummaryDTO;
import com.evs.electricvehiclestore.dto.PaymentResultDTO;
import com.evs.electricvehiclestore.dto.ShippingInfoDTO;
import com.evs.electricvehiclestore.entity.Cart;
import com.evs.electricvehiclestore.entity.CartItem;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.CartItemRepository;
import com.evs.electricvehiclestore.repository.CartRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import com.evs.electricvehiclestore.service.OrderService;

/**
 * @author Uzma Alam
 * End-to-end tests for UC7 (Checkout), UC8 (Make Payment), and UC9 
 */
@SpringBootTest
class OrderCheckoutPaymentTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private static final Long TEST_USER_ID = 9001L;

    private Cart seedCartWithOneVehicle(double price) {
        return seedCartWithOneVehicle(TEST_USER_ID, price);
    }

    // userId must be unique per cart - findByUserId() expects at most one cart per user
    private Cart seedCartWithOneVehicle(long userId, double price) {
        Vehicle vehicle = vehicleRepository.save(
                new Vehicle("Tesla", "Model 3", 2024, price, 5000, "Sedan", false));
        Cart cart = cartRepository.save(new Cart(userId));
        cartItemRepository.save(new CartItem(cart.getId(), vehicle.getId(), 1));
        return cart;
    }

    private ShippingInfoDTO validShippingInfo() {
        ShippingInfoDTO shipping = new ShippingInfoDTO();
        shipping.setStreet("123 Main St");
        shipping.setCity("Toronto");
        shipping.setProvince("ON");
        shipping.setCountry("Canada");
        shipping.setZip("M1C 6K5");
        shipping.setPhone("416-123-4567");
        return shipping;
    }

    private CreditCardDTO validCreditCard() {
        CreditCardDTO card = new CreditCardDTO();
        card.setCardHolderName("Uzma Alam");
        card.setCardNumber("4111111111111111");
        card.setExpiryMonth("12");
        card.setExpiryYear("2030");
        card.setCvv("123");
        return card;
    }

    // UC7: Checkout

    @Test
    @Transactional
    void checkout_createsOrderWithCorrectTotalAndPendingStatus() {
        seedCartWithOneVehicle(48000);

        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(TEST_USER_ID);
        request.setShippingInfo(validShippingInfo());

        OrderSummaryDTO summary = orderService.checkout(request);

        assertNotNull(summary.getOrderId());
        assertEquals("PENDING_PAYMENT", summary.getStatus());
        assertEquals(48000, summary.getTotalAmount());
        assertEquals(1, summary.getItems().size());
        assertEquals("Toronto", summary.getShippingInfo().getCity());
    }

    @Test
    @Transactional
    void checkout_throwsWhenCartIsEmpty() {
        cartRepository.save(new Cart(TEST_USER_ID)); // cart exists but has no items

        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(TEST_USER_ID);
        request.setShippingInfo(validShippingInfo());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.checkout(request));
        assertTrue(ex.getReason().toLowerCase().contains("empty"));
    }

    @Test
    @Transactional
    void checkout_throwsWhenNoCartExistsForUser() {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(999999L); // no cart ever created for this user
        request.setShippingInfo(validShippingInfo());

        assertThrows(ResponseStatusException.class, () -> orderService.checkout(request));
    }

    //  UC8 + UC9: Payment / credit card 

    @Test
    @Transactional
    void confirmOrder_deniesExpiredCard() {
        seedCartWithOneVehicle(30000);
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(TEST_USER_ID);
        request.setShippingInfo(validShippingInfo());
        OrderSummaryDTO summary = orderService.checkout(request);

        CreditCardDTO expiredCard = validCreditCard();
        expiredCard.setExpiryYear("2020");

        assertThrows(ResponseStatusException.class,
                () -> orderService.confirmOrder(summary.getOrderId(), expiredCard));
    }

    @Test
    @Transactional
    void confirmOrder_rejectsPayingTwice() {
        seedCartWithOneVehicle(30000);
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(TEST_USER_ID);
        request.setShippingInfo(validShippingInfo());
        OrderSummaryDTO summary = orderService.checkout(request);

        orderService.confirmOrder(summary.getOrderId(), validCreditCard()); // 1st attempt, consumes a gateway slot

        assertThrows(ResponseStatusException.class,
                () -> orderService.confirmOrder(summary.getOrderId(), validCreditCard()));
    }

    /**
        * This test verifies the mock payment gateway rule from the project spec
     */
    @Test
    @Transactional
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void confirmOrder_honorsFirstTwoThenDeniesThird() {
        Long order1 = orderService.checkout(checkoutRequestFor(seedCartWithOneVehicle(9101L, 10000))).getOrderId();
        Long order2 = orderService.checkout(checkoutRequestFor(seedCartWithOneVehicle(9102L, 20000))).getOrderId();
        Long order3 = orderService.checkout(checkoutRequestFor(seedCartWithOneVehicle(9103L, 30000))).getOrderId();

        PaymentResultDTO result1 = orderService.confirmOrder(order1, validCreditCard());
        PaymentResultDTO result2 = orderService.confirmOrder(order2, validCreditCard());
        PaymentResultDTO result3 = orderService.confirmOrder(order3, validCreditCard());

        assertTrue(result1.isApproved(), "1st payment should be approved");
        assertEquals("Order Successfully Completed.", result1.getMessage());

        assertTrue(result2.isApproved(), "2nd payment should be approved");

        assertFalse(result3.isApproved(), "3rd payment should be denied");
        assertEquals("Credit Card Authorization Failed.", result3.getMessage());

        assertEquals("PROCESSED", orderService.getOrder(order1).getStatus());
        assertEquals("PROCESSED", orderService.getOrder(order2).getStatus());
        assertEquals("DENIED", orderService.getOrder(order3).getStatus());
    }

    private CheckoutRequest checkoutRequestFor(Cart cart) {
        CheckoutRequest request = new CheckoutRequest();
        request.setUserId(cart.getUserId());
        request.setShippingInfo(validShippingInfo());
        return request;
    }
}