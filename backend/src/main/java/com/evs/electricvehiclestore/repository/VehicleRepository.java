package com.evs.electricvehiclestore.repository;

import com.evs.electricvehiclestore.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByBrandIgnoreCase(String brand);

    List<Vehicle> findByShapeIgnoreCase(String shape);

    List<Vehicle> findByModelYear(int modelYear);

    List<Vehicle> findByHotDealTrue();
}