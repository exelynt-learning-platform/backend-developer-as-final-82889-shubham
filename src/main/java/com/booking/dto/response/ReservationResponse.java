package com.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.booking.enums.ReservationStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponse {

    private Long id;

    private Long userId;

    private Long resourceId;

    private String resourceName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal price;

    private ReservationStatus status;
}