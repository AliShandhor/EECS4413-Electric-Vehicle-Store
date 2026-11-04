package com.evs.electricvehiclestore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.entity.VehicleImage;
import com.evs.electricvehiclestore.dto.VehicleRequest;
import com.evs.electricvehiclestore.service.CatalogService;
import jakarta.validation.Valid;

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

    @GetMapping("/vehicles/search")
    public List<Vehicle> searchVehicles(
            @RequestParam String keyword
    ) {
        return catalogService.searchVehicles(keyword);
    }

    @GetMapping("/vehicles/price-range")
    public List<Vehicle> filterByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice
    ) {
        return catalogService.filterByPriceRange(minPrice, maxPrice);
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

    @PostMapping("/vehicles")
    public Vehicle addVehicle(@Valid @RequestBody VehicleRequest request) {
        return catalogService.addVehicle(request);
    }

    @PostMapping(
            value = "/vehicles/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Vehicle uploadVehicleImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image
    ) {
        return catalogService.saveVehicleImage(id, image);
    }

    @GetMapping("/vehicles/{id}/image")
    public ResponseEntity<byte[]> getVehicleImage(@PathVariable Long id) {
        VehicleImage image = catalogService.getVehicleImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getData().length)
                .cacheControl(CacheControl.noCache())
                .body(image.getData());
    }
}
