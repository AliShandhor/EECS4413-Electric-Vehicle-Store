package com.evs.electricvehiclestore.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.UserDTO;
import com.evs.electricvehiclestore.service.IdentityService;

@RestController
@RequestMapping("/api/identity")
@CrossOrigin(origins = "*") // Allows your React front-end to connect cleanly
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    // UC1: POST /api/identity/register (Page 30)
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody UserDTO userDTO) {
        try {
            User registeredUser = identityService.register(userDTO);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // UC2: POST /api/identity/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            String token = identityService.login(email, password);
            
            // Returns token inside a JSON payload object for easy frontend extraction
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    // UC3: POST /api/identity/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        identityService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out."));
    }
}
