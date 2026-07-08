package com.evs.electricvehiclestore.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evs.electricvehiclestore.dto.CheckoutRequest;
import com.evs.electricvehiclestore.dto.OrderSummaryDTO;
import com.evs.electricvehiclestore.dto.PaymentRequest;
import com.evs.electricvehiclestore.dto.PaymentResultDTO;
import com.evs.electricvehiclestore.service.OrderService;

import jakarta.validation.Valid;

/**
 * @author Uzma Alam
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    
    // customer's cart and record shipping info
    @PostMapping("/checkout")
    public ResponseEntity<OrderSummaryDTO> checkout(@Valid @RequestBody CheckoutRequest request) {
        OrderSummaryDTO summary = orderService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    // process payment for a pending order
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<PaymentResultDTO> confirmOrder(@PathVariable Long orderId,
                                                           @Valid @RequestBody PaymentRequest request) {
        PaymentResultDTO result = orderService.confirmOrder(orderId, request.getCreditCard());
        HttpStatus status = result.isApproved() ? HttpStatus.OK : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderSummaryDTO> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }
}