package com.evs.electricvehiclestore.repository;

import com.evs.electricvehiclestore.entity.SavedVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedVehicleRepository
        extends JpaRepository<SavedVehicle, Long> {

    List<SavedVehicle> findByUserIdOrderByIdDesc(Long userId);

    Optional<SavedVehicle> findByUserIdAndVehicleId(
            Long userId,
            Long vehicleId
    );

    boolean existsByUserIdAndVehicleId(
            Long userId,
            Long vehicleId
    );
}