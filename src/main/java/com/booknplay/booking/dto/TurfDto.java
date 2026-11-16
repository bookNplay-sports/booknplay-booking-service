package com.booknplay.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class TurfDto {
    private Long id;
    private String name;
    private String city;
    private String area;
    private String sports;
    private BigDecimal basePrice;
    private LocalTime openingTime;
    private LocalTime closingTime;
}