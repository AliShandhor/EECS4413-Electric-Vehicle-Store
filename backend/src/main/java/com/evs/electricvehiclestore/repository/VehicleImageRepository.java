package com.evs.electricvehiclestore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.VehicleImage;

public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {
}
