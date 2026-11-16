package com.booknplay.booking.service;

import com.booknplay.booking.dto.AvailableSlotDto;
import com.booknplay.booking.dto.BookingRequest;
import com.booknplay.booking.dto.TurfDto;
import com.booknplay.booking.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Booking createBooking(BookingRequest request);
    List<Booking> getUpcomingBookings(Long userId);
    List<Booking> getPastBookings(Long userId);
    Booking cancelBooking(Long bookingId);
    List<TurfDto> searchTurfs(
            String city,
            String area,
            String sport,
            LocalDate bookingDate,
            BigDecimal maxPrice
    );

    List<AvailableSlotDto> getAvailableSlots(
            Long turfId,
            LocalDate bookingDate
    );
}
