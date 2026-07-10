package com.evs.electricvehiclestore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.evs.electricvehiclestore.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Used for UC1 to check duplicate registrations
    boolean existsByEmail(String email);
    
    // Used for UC2 to find user credentials during login
    Optional<User> findByEmail(String email);
}