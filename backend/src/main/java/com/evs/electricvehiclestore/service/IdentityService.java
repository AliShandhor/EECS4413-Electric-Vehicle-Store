package com.evs.electricvehiclestore.service;

import com.evs.electricvehiclestore.config.JwtUtil;
import com.evs.electricvehiclestore.config.TokenBlacklist;
import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.UserDTO;
import com.evs.electricvehiclestore.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public IdentityService(UserRepository userRepository, JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    // UC1: Register
    @Transactional
    public User register(UserDTO userDTO) {
        if (userDTO == null || userDTO.fullName() == null || userDTO.fullName().isBlank()
                || userDTO.email() == null || userDTO.email().isBlank()
                || userDTO.password() == null || userDTO.password().length() < 8) {
            throw new IllegalArgumentException(
                    "Full name, a valid email, and a password of at least 8 characters are required.");
        }
        String normalizedEmail = userDTO.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Registration failed: Email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(userDTO.password());

        User newUser = new User(
            userDTO.fullName().trim(),
            normalizedEmail,
            hashedPassword,
            "CUSTOMER"
        );

        return userRepository.save(newUser);
    }

    // UC2: Sign In — returns a real signed JWT
    public Map<String, Object> login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        String normalizedEmail = email.trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of(
            "token", token,
            "expiresInSeconds", jwtUtil.getExpirationSeconds(),
            "user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole()
            )
        );
    }

    // UC3: Sign Out — revokes the token server-side
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenBlacklist.revoke(token);
        }
    }

    // Token validation — used by protected endpoints
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        if (tokenBlacklist.isRevoked(token)) return false;
        return jwtUtil.isValid(token);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }
}
