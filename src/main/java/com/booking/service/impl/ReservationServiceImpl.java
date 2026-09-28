package com.booking.service.impl;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import com.booking.exeption.BadRequestException;
import com.booking.exeption.ResourceNotFoundException;
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

        // 1. Get currently authenticated user
        User currentUser = getAuthenticatedUser();

        // 2. Find requested resource
        Resource resource = resourceRepository
                .findById(request.getResourceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.getResourceId()
                        ));

        // 3. Validate time
        validateTime(
                request.getStartTime(),
                request.getEndTime()
        );

        // 4. Check resource availability
        if (!resource.isAvailable()) {
            throw new BadRequestException(
                    "Resource is currently unavailable"
            );
        }

        // 5. Check overlapping reservation
        boolean overlapping = reservationRepository
                .existsOverlappingReservation(
                        resource.getId(),
                        request.getStartTime(),
                        request.getEndTime()
                );

        if (overlapping) {
            throw new BadRequestException(
                    "Resource is already reserved for the selected time"
            );
        }

        // 6. Create reservation
        Reservation reservation = Reservation.builder()
                .user(currentUser)
                .resource(resource)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(resource.getPrice())
                .status(ReservationStatus.PENDING)
                .build();

        // 7. Save
        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {

        User currentUser = getAuthenticatedUser();

        Reservation reservation = getReservation(id);

        // USER can only access own reservation.
        // ADMIN can access any reservation.
        if (!isAdmin(currentUser)
                && !reservation.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new BadRequestException(
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
    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new BadRequestException(
                    "minPrice cannot be greater than maxPrice");
        }

        Specification<Reservation> specification =
                ReservationSpecification.filter(
                        status,
                        minPrice,
                        maxPrice
                );

        return reservationRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ReservationResponse updateReservationStatus(
            Long id,
            ReservationStatus status) {

        Reservation reservation = getReservation(id);

        validateStatusTransition(
                reservation.getStatus(),
                status
        );

        reservation.setStatus(status);

        return mapToResponse(reservation);
    }

    @Override
    public void cancelReservation(Long id) {

        Reservation reservation = getReservation(id);

        User currentUser = getAuthenticatedUser();

        // USER can cancel only own reservation.
        if (!isAdmin(currentUser)
                && !reservation.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new BadRequestException(
                    "You are not allowed to cancel this reservation"
            );
        }

        if (reservation.getStatus()
                == ReservationStatus.CANCELLED) {

            throw new BadRequestException(
                    "Reservation is already cancelled"
            );
        }

        reservation.setStatus(
                ReservationStatus.CANCELLED
        );
    }

    @Override
    public void deleteReservation(Long id) {

        Reservation reservation = getReservation(id);

        reservationRepository.delete(reservation);
    }

    // --------------------------------------------------
    // Helper methods
    // --------------------------------------------------

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new BadRequestException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        ));
    }

    private Reservation getReservation(Long id) {

        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation not found with id: " + id
                        ));
    }

    private boolean isAdmin(User user) {

        return user.getRole().name().equals("ADMIN");
    }

    private void validateTime(
            java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {

        if (!startTime.isBefore(endTime)) {

            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }
    }

    private void validateStatusTransition(
            ReservationStatus currentStatus,
            ReservationStatus newStatus) {

        if (currentStatus
                == ReservationStatus.CANCELLED) {

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