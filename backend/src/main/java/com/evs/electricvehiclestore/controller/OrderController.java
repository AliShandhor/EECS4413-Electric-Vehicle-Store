package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public String checkout() {
        return orderService.checkout();
    }

    @PostMapping("/confirm")
    public String confirmOrder() {
        return orderService.confirmOrder();
    }
}