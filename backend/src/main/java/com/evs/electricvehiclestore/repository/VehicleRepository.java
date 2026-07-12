package com.evs.electricvehiclestore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByBrandIgnoreCase(String brand);

    List<Vehicle> findByShapeIgnoreCase(String shape);

    List<Vehicle> findByModelYear(int modelYear);

    List<Vehicle> findByHotDealTrue();

    List<Vehicle> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
            String brandKeyword,
            String modelKeyword
    );

    List<Vehicle> findByPriceBetween(double minimumPrice, double maximumPrice);
}