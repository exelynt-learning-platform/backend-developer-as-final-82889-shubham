package com.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.booking.entity.User;
import com.booking.enums.Role;
import com.booking.security.JwtService;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "testSecretKeyForResourceBookingSystemJwt2026VerySecure";

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                SECRET,
                3600000L
        );
    }

    @Test
    void generateToken_shouldGenerateTokenSuccessfully() {

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("user@booking.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_shouldContainUserEmail() {

        User user = User.builder()
                .id(1L)
                .email("user@booking.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals(
                "user@booking.com",
                email
        );
    }

    @Test
    void generateToken_shouldContainAdminEmail() {

        User admin = User.builder()
                .id(2L)
                .email("admin@booking.com")
                .role(Role.ADMIN)
                .build();

        String token = jwtService.generateToken(admin);

        assertEquals(
                "admin@booking.com",
                jwtService.extractEmail(token)
        );
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {

        User user = User.builder()
                .id(1L)
                .email("user@booking.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertTrue(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void isTokenValid_shouldReturnFalse_forInvalidToken() {

        String token = "invalid.jwt.token";

        assertFalse(
                jwtService.isTokenValid(token)
        );
    }

    @Test
    void isTokenValid_shouldReturnFalse_forEmptyToken() {

        assertFalse(
                jwtService.isTokenValid("")
        );
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {

        User user = User.builder()
                .id(10L)
                .email("test@booking.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertEquals(
                "test@booking.com",
                jwtService.extractEmail(token)
        );
    }
}