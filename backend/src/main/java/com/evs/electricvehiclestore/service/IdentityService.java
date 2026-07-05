package com.evs.electricvehiclestore.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.UserDTO;
import com.evs.electricvehiclestore.repository.UserRepository;

@Service
public class IdentityService {

    private final UserRepository userRepository;

    public IdentityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // UC1: Register Customer
    public User register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.email())) {
            throw new IllegalArgumentException("Registration failed: Email already exists.");
        }
        
        User newUser = new User(
            userDTO.fullName(),
            userDTO.email(),
            userDTO.password(),
            userDTO.role() == null ? "CUSTOMER" : userDTO.role()
        );
        
        return userRepository.save(newUser);
    }

    // UC2: Sign In
    // Returns a dummy session token string since security architectures aren't integrated yet
    public String login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            // Generates a mock token to fit your Stateless REST specification (Page 6)
            return "mock-jwt-token-for-" + userOpt.get().getId();
        }
        
        throw new IllegalArgumentException("Invalid email or password.");
    }

    // UC3: Sign Out
    public void logout(String token) {
        // Stateless APIs do not store sessions on the server (Page 6).
        // Clients simply destroy the token on their end.
        System.out.println("Token invalidated successfully: " + token);
    }

    // Diagnostic validation method requested in requirements documentation
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
