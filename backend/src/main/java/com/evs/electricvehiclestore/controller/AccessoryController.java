package com.evs.electricvehiclestore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evs.electricvehiclestore.entity.Accessory;
import com.evs.electricvehiclestore.repository.AccessoryRepository;

@RestController
@RequestMapping("/api/accessories")
public class AccessoryController {

    private final AccessoryRepository accessoryRepository;

    public AccessoryController(AccessoryRepository accessoryRepository) {
        this.accessoryRepository = accessoryRepository;
    }

    @GetMapping
    public List<Accessory> listAvailableAccessories() {
        return accessoryRepository.findByAvailableTrueOrderByPriceAsc();
    }
}
