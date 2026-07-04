package com.evs.electricvehiclestore.service;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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
        return vehicleRepository.findById(vehicleId).orElse(null);
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