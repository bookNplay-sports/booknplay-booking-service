package com.booknplay.booking.controller;

import com.booknplay.booking.dto.AvailableSlotDto;
import com.booknplay.booking.dto.BookingRequest;
import com.booknplay.booking.dto.TurfDto;
import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<List<Booking>> getUpcomingBookings(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getUpcomingBookings(userId)
        );
    }
    @GetMapping("/past/{userId}")
    public ResponseEntity<List<Booking>> getPastBookings(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getPastBookings(userId)
        );
    }
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId)
        );
    }

    @GetMapping("/search-turfs")
    public ResponseEntity<List<TurfDto>> searchTurfs(
            @RequestParam String city,
            @RequestParam String area,
            @RequestParam String sport,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate bookingDate,
            @RequestParam BigDecimal maxPrice
    ) {
        return ResponseEntity.ok(
                bookingService.searchTurfs(
                        city, area, sport, bookingDate, maxPrice
                )
        );
    }
    @GetMapping("/available-slots")
    public ResponseEntity<List<AvailableSlotDto>> getAvailableSlots(
            @RequestParam Long turfId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate bookingDate) {
        return ResponseEntity.ok(
                bookingService.getAvailableSlots(turfId, bookingDate)
        );
    }
}
