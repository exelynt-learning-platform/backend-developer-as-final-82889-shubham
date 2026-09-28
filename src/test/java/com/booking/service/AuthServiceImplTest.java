package com.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.booking.dto.request.LoginRequest;
import com.booking.dto.response.LoginResponse;
import com.booking.entity.User;
import com.booking.enums.Role;
import com.booking.repository.UserRepository;
import com.booking.security.JwtService;
import com.booking.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {

        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("user@booking.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        LoginRequest request = new LoginRequest();
        request.setEmail("user@booking.com");
        request.setPassword("user123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());

        verify(userRepository).findByEmail(request.getEmail());

        verify(passwordEncoder).matches(
                request.getPassword(),
                user.getPassword());

        verify(jwtService).generateToken(user);
    }
}