package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.dto.CartResponse;
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
    public CartResponse getCart(@RequestParam Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping({"/add", "/items"})
    public CartResponse addItem(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return cartService.addItem(userId, vehicleId, quantity);
    }

    @PutMapping("/update")
    public CartResponse updateQuantity(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam int quantity
    ) {
        return cartService.updateQuantity(userId, vehicleId, quantity);
    }

    @PutMapping("/items/{vehicleId}")
    public CartResponse updateQuantityRestfully(
            @RequestParam Long userId,
            @PathVariable Long vehicleId,
            @RequestParam int quantity
    ) {
        return cartService.updateQuantity(userId, vehicleId, quantity);
    }

    @DeleteMapping("/remove")
    public CartResponse removeItem(
            @RequestParam Long userId,
            @RequestParam Long vehicleId
    ) {
        return cartService.removeItem(userId, vehicleId);
    }

    @DeleteMapping("/items/{vehicleId}")
    public CartResponse removeItemRestfully(
            @RequestParam Long userId,
            @PathVariable Long vehicleId
    ) {
        return cartService.removeItem(userId, vehicleId);
    }

    @PostMapping("/saved")
    public CartResponse saveForLater(
            @RequestParam Long userId,
            @RequestParam Long vehicleId
    ) {
        return cartService.saveForLater(userId, vehicleId);
    }

    @PostMapping("/items/{vehicleId}/save-for-later")
    public CartResponse moveCartItemToSaved(
            @RequestParam Long userId,
            @PathVariable Long vehicleId
    ) {
        return cartService.saveForLater(userId, vehicleId);
    }

    @PostMapping("/saved/{vehicleId}/move-to-cart")
    public CartResponse moveSavedVehicleToCart(
            @RequestParam Long userId,
            @PathVariable Long vehicleId
    ) {
        return cartService.moveSavedVehicleToCart(userId, vehicleId);
    }

    @DeleteMapping("/saved/{vehicleId}")
    public CartResponse removeSavedVehicle(
            @RequestParam Long userId,
            @PathVariable Long vehicleId
    ) {
        return cartService.removeSavedVehicle(userId, vehicleId);
    }
}