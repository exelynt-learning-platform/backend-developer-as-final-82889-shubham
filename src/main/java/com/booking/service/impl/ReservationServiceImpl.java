package com.booking.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.entity.Reservation;
import com.booking.entity.Resource;
import com.booking.entity.User;
import com.booking.enums.ReservationStatus;
import com.booking.exception.BadRequestException;
import com.booking.exception.ResourceNotFoundException;
import com.booking.repository.ReservationRepository;
import com.booking.repository.ResourceRepository;
import com.booking.repository.UserRepository;
import com.booking.service.ReservationService;
import com.booking.specification.ReservationSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Override
    public ReservationResponse createReservation(
            ReservationRequest request) {

        User user = getAuthenticatedUser();

        Resource resource = resourceRepository
                .findByIdForUpdate(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        ));

        validateReservationTime(
                request.getStartTime(),
                request.getEndTime()
        );

        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

        boolean overlapping =
                reservationRepository.existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

        if (overlapping) {
            throw new BadRequestException(
                    "Resource is already reserved for the selected time"
            );
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(resource.getPrice())
                .status(ReservationStatus.PENDING)
                .build();

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {

        User currentUser = getAuthenticatedUser();

        Reservation reservation = findReservation(id);

        /*
         * ADMIN can view any reservation.
         * USER can view only their own reservation.
         */
        if (!isAdmin(currentUser)
                && !reservation.getUser().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this reservation"
            );
        }

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations() {

        User currentUser = getAuthenticatedUser();

        return reservationRepository
                .findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new BadRequestException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        return reservationRepository
                .findAll(
                        ReservationSpecification.filter(
                                status,
                                minPrice,
                                maxPrice
                        ),
                        pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse updateReservationStatus(
            Long id,
            ReservationStatus status) {

        Reservation reservation = findReservation(id);

        if (status == null) {
            throw new BadRequestException(
                    "Reservation status is required"
            );
        }

        validateStatusTransition(
                reservation.getStatus(),
                status
        );

        reservation.setStatus(status);

        return mapToResponse(reservation);
    }

    @Override
    public void cancelReservation(Long id) {

        User currentUser = getAuthenticatedUser();

        Reservation reservation = findReservation(id);

        /*
         * ADMIN can cancel any reservation.
         * USER can cancel only their own reservation.
         */
        if (!isAdmin(currentUser)
                && !reservation.getUser().getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not allowed to cancel this reservation"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new BadRequestException(
                    "Reservation is already cancelled"
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteReservation(Long id) {

        Reservation reservation = findReservation(id);

        reservationRepository.delete(reservation);
    }

    // =========================================================
    // Helper methods
    // =========================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        ));
    }

    private Reservation findReservation(Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        ));
    }

    private boolean isAdmin(User user) {

        return user.getRole() != null
                && user.getRole().name().equals("ADMIN");
    }

    private void validateReservationTime(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (!startTime.isBefore(endTime)) {

            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }
    }

    private void validateStatusTransition(
            ReservationStatus currentStatus,
            ReservationStatus newStatus) {

        if (currentStatus == ReservationStatus.CANCELLED) {

            throw new BadRequestException(
                    "Cancelled reservation cannot be modified"
            );
        }

        if (currentStatus == newStatus) {

            throw new BadRequestException(
                    "Reservation already has status "
                            + newStatus
            );
        }
    }

    private ReservationResponse mapToResponse(
            Reservation reservation) {

        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .price(reservation.getPrice())
                .status(reservation.getStatus())
                .build();
    }
}