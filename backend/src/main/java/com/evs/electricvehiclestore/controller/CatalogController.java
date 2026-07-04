package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/vehicles")
    public List<Vehicle> listVehicles() {
        return catalogService.listVehicles();
    }

    @GetMapping("/vehicles/{id}")
    public Vehicle getVehicle(@PathVariable Long id) {
        return catalogService.getVehicle(id);
    }

    @GetMapping("/vehicles/brand/{brand}")
    public List<Vehicle> filterByBrand(@PathVariable String brand) {
        return catalogService.filterByBrand(brand);
    }

    @GetMapping("/vehicles/shape/{shape}")
    public List<Vehicle> filterByShape(@PathVariable String shape) {
        return catalogService.filterByShape(shape);
    }

    @GetMapping("/vehicles/year/{year}")
    public List<Vehicle> filterByYear(@PathVariable int year) {
        return catalogService.filterByYear(year);
    }

    @GetMapping("/vehicles/sort/price")
    public List<Vehicle> sortByPrice() {
        return catalogService.sortByPrice();
    }

    @GetMapping("/vehicles/sort/mileage")
    public List<Vehicle> sortByMileage() {
        return catalogService.sortByMileage();
    }

    @GetMapping("/vehicles/hot-deals")
    public List<Vehicle> getHotDeals() {
        return catalogService.getHotDeals();
    }
}