package com.booking.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private String type;
    private BigDecimal price;
    private boolean available;
}