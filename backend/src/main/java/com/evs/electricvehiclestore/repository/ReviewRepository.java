package com.evs.electricvehiclestore.repository;

import com.evs.electricvehiclestore.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByVehicleId(Long vehicleId);
}