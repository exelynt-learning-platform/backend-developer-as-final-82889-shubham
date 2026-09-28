package com.booking.service;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.booking.enums.ReservationStatus;
import com.booking.specification.ReservationSpecification;

class ReservationSpecificationTest {

    @Test
    void filter_shouldReturnSpecification_whenNoFiltersProvided() {

        Specification<?> specification =
                ReservationSpecification.filter(
                        null,
                        null,
                        null
                );

        assertNotNull(specification);
    }


    @Test
    void filter_shouldReturnSpecification_whenStatusProvided() {

        Specification<?> specification =
                ReservationSpecification.filter(
                        ReservationStatus.PENDING,
                        null,
                        null
                );

        assertNotNull(specification);
    }


    @Test
    void filter_shouldReturnSpecification_whenMinPriceProvided() {

        Specification<?> specification =
                ReservationSpecification.filter(
                        null,
                        new BigDecimal("500"),
                        null
                );

        assertNotNull(specification);
    }


    @Test
    void filter_shouldReturnSpecification_whenMaxPriceProvided() {

        Specification<?> specification =
                ReservationSpecification.filter(
                        null,
                        null,
                        new BigDecimal("2000")
                );

        assertNotNull(specification);
    }


    @Test
    void filter_shouldReturnSpecification_whenAllFiltersProvided() {

        Specification<?> specification =
                ReservationSpecification.filter(
                        ReservationStatus.CONFIRMED,
                        new BigDecimal("500"),
                        new BigDecimal("2000")
                );

        assertNotNull(specification);
    }
}