package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String getCart() {
        return cartService.getCart();
    }

    @PostMapping("/add")
    public String addItem() {
        return cartService.addItem();
    }

    @DeleteMapping("/remove")
    public String removeItem() {
        return cartService.removeItem();
    }

    @PutMapping("/update")
    public String updateQuantity() {
        return cartService.updateQuantity();
    }
}