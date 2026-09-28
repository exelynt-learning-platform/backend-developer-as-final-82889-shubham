package com.booking.service;

import com.booking.dto.request.ReservationRequest;
import com.booking.dto.response.ReservationResponse;
import com.booking.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(
            ReservationRequest request
    );

    ReservationResponse getReservationById(
            Long id
    );

    List<ReservationResponse> getMyReservations();

    Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    ReservationResponse updateReservationStatus(
            Long id,
            ReservationStatus status
    );

    void cancelReservation(
            Long id
    );

    void deleteReservation(
            Long id
    );
}