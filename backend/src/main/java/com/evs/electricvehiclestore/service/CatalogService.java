package com.evs.electricvehiclestore.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@Service
public class CatalogService {

    private final VehicleRepository vehicleRepository;

    public CatalogService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> listVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + vehicleId
                ));
    }

    public List<Vehicle> filterByBrand(String brand) {
        return vehicleRepository.findByBrandIgnoreCase(brand);
    }

    public List<Vehicle> filterByShape(String shape) {
        return vehicleRepository.findByShapeIgnoreCase(shape);
    }

    public List<Vehicle> filterByYear(int year) {
        return vehicleRepository.findByModelYear(year);
    }

    public List<Vehicle> searchVehicles(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A search keyword is required"
            );
        }

        String cleanedKeyword = keyword.trim();

        return vehicleRepository
                .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                        cleanedKeyword,
                        cleanedKeyword
                );
    }

    public List<Vehicle> filterByPriceRange(
            double minimumPrice,
            double maximumPrice
    ) {
        if (minimumPrice < 0 || maximumPrice < minimumPrice) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The price range is invalid"
            );
        }

        return vehicleRepository.findByPriceBetween(
                minimumPrice,
                maximumPrice
        );
    }

    public List<Vehicle> sortByPrice() {
        return vehicleRepository.findAll()
                .stream()
                .sorted(Comparator.comparingDouble(Vehicle::getPrice))
                .toList();
    }

    public List<Vehicle> sortByMileage() {
        return vehicleRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Vehicle::getMileage))
                .toList();
    }

    public List<Vehicle> getHotDeals() {
        return vehicleRepository.findByHotDealTrue();
    }
}