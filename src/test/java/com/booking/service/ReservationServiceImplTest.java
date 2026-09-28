package com.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.booking.dto.response.ReservationResponse;
import com.booking.entity.Reservation;
import com.booking.entity.Resource;
import com.booking.entity.User;
import com.booking.enums.ReservationStatus;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.service.impl.ReservationServiceImpl;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User admin;
    private User user;
    private User anotherUser;
    private Resource resource;
    private Reservation reservation;

    @BeforeEach
    void setUp() {

        admin = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@booking.com")
                .role(com.booking.enums.Role.ADMIN)
                .build();

        user = User.builder()
                .id(2L)
                .name("User")
                .email("user@booking.com")
                .role(com.booking.enums.Role.USER)
                .build();

        anotherUser = User.builder()
                .id(3L)
                .name("Another User")
                .email("another@booking.com")
                .role(com.booking.enums.Role.USER)
                .build();

        resource = Resource.builder()
                .id(10L)
                .name("Conference Room")
                .type("ROOM")
                .price(new BigDecimal("500.00"))
                .available(true)
                .build();

        reservation = Reservation.builder()
                .id(100L)
                .user(user)
                .resource(resource)
                .startTime(
                        java.time.LocalDateTime.of(
                                2026, 9, 29, 10, 0
                        )
                )
                .endTime(
                        java.time.LocalDateTime.of(
                                2026, 9, 29, 11, 0
                        )
                )
                .price(new BigDecimal("500.00"))
                .status(ReservationStatus.PENDING)
                .build();
    }

    @Test
    void userCanAccessOwnReservation() {

        authenticateAs(user);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        ReservationResponse response =
                reservationService.getReservationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(2L, response.getUserId());
    }

    @Test
    void userCannotAccessAnotherUsersReservation() {

        authenticateAs(anotherUser);

        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                AccessDeniedException.class,
                () -> reservationService.getReservationById(100L)
        );
    }

    @Test
    void adminCanAccessAnyReservation() {

        authenticateAs(admin);

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        ReservationResponse response =
                reservationService.getReservationById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void adminCanDeleteReservation() {

        authenticateAs(admin);

        when(reservationRepository.findById(100L))
                .thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(100L);

        verify(reservationRepository)
                .delete(reservation);
    }

    @Test
    void reservationNotFoundThrowsException() {

        authenticateAs(user);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                com.booking.exeption.ResourceNotFoundException.class,
                () -> reservationService.getReservationById(999L)
        );
    }

    private void authenticateAs(User user) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        java.util.Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}