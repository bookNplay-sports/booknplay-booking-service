package com.booknplay.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class BookingRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long turfId;
    @NotNull
    private LocalDate bookingDate;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
}
