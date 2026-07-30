package com.evs.electricvehiclestore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.Accessory;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {
    List<Accessory> findByAvailableTrueOrderByPriceAsc();
}
