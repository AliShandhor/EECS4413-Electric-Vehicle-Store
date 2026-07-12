package com.evs.electricvehiclestore.service;

import com.evs.electricvehiclestore.config.JwtUtil;
import com.evs.electricvehiclestore.config.TokenBlacklist;
import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.UserDTO;
import com.evs.electricvehiclestore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdentityServiceTest {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private TokenBlacklist tokenBlacklist;
    private IdentityService identityService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        tokenBlacklist = mock(TokenBlacklist.class);
        identityService = new IdentityService(userRepository, jwtUtil, tokenBlacklist);
    }

    // AT-01: Register succeeds with valid data
    @Test
    void register_returnsUser_whenEmailIsNew() {
        UserDTO dto = new UserDTO("Jane Doe", "jane@example.com", "Password1!", "CUSTOMER");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = identityService.register(dto);

        assertEquals("jane@example.com", result.getEmail());
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("CUSTOMER", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    // AT-02: Registration fails when email already exists
    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        UserDTO dto = new UserDTO("Jane Doe", "jane@example.com", "Password1!", "CUSTOMER");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> identityService.register(dto)
        );

        assertEquals("Registration failed: Email already exists.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // AT-03: Login succeeds with correct credentials and returns a token
    @Test
    void login_returnsToken_whenCredentialsAreCorrect() {
        String hashedPassword = encoder.encode("Password1!");
        User user = new User("Jane Doe", "jane@example.com", hashedPassword, "CUSTOMER");
        user.setId(1L);

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("jane@example.com")).thenReturn("mock.jwt.token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

        var result = identityService.login("jane@example.com", "Password1!");

        assertEquals("mock.jwt.token", result.get("token"));
    }

    // AT-04: Logout revokes the token so validate returns false
    @Test
    void logout_revokesToken_andValidateReturnsFalse() {
        String token = "mock.jwt.token";

        identityService.logout(token);

        verify(tokenBlacklist).revoke(token);

        when(tokenBlacklist.isRevoked(token)).thenReturn(true);
        assertFalse(identityService.validateToken(token));
    }
}