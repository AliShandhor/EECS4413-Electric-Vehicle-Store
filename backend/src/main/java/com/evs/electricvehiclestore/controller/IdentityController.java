package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.UserDTO;
import com.evs.electricvehiclestore.service.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    // UC1: Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            User registered = identityService.register(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", registered.getId(),
                "fullName", registered.getFullName(),
                "email", registered.getEmail(),
                "role", registered.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // UC2: Sign In
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            Map<String, Object> response = identityService.login(email, password);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    // UC3: Sign Out
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        identityService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out."));
    }

    // Token validation
    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        boolean valid = identityService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }

    // Email existence check
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(Map.of("exists", identityService.checkEmailExists(email)));
    }
}