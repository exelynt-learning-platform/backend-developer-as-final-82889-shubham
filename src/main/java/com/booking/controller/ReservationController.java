package com.booking.controller;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.enums.ReservationStatus;
import com.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // USER creates a reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    // USER gets only their own reservations
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {

        return ResponseEntity.ok(
                reservationService.getMyReservations()
        );
    }

    // USER can get their own reservation.
    // ADMIN can get any reservation.
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                reservationService.getReservationById(id)
        );
    }

    // ADMIN only
    // Supports status, minPrice, maxPrice, pagination and sorting.
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @RequestParam(required = false)
            ReservationStatus status,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                reservationService.getAllReservations(
                        status,
                        minPrice,
                        maxPrice,
                        pageable
                )
        );
    }

    // ADMIN only
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {

        return ResponseEntity.ok(
                reservationService.updateReservationStatus(
                        id,
                        status
                )
        );
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id) {

        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}