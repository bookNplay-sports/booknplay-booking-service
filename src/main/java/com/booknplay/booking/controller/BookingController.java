package com.booknplay.booking.controller;

import com.booknplay.booking.dto.AvailableSlotDto;
import com.booknplay.booking.dto.BookingRequest;
import com.booknplay.booking.dto.TurfDto;
import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Booking Management",
        description = "APIs for booking management, turf search, and available slot retrieval"
)
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Create a new booking",
            description = "Allows a user to create a new booking for a turf."
    )
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
    @Operation(
            summary = "Get upcoming bookings",
            description = "Retrieves all upcoming bookings for a specific user."
    )
    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<List<Booking>> getUpcomingBookings(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getUpcomingBookings(userId)
        );
    }
    @Operation(
            summary = "Get past bookings",
            description = "Retrieves all past bookings for a specific user."
    )
    @GetMapping("/past/{userId}")
    public ResponseEntity<List<Booking>> getPastBookings(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                bookingService.getPastBookings(userId)
        );
    }
    @Operation(
            summary = "Cancel a booking",
            description = "Allows a user to cancel an existing booking."
    )
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId)
        );
    }

    @Operation(
            summary = "Search for available turfs",
            description = "Allows a user to search for available turfs based on location and date."
    )
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
    @Operation(
            summary = "Get available slots",
            description = "Retrieves all available slots for a specific turf on a given date."
    )
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

    @Operation(
            summary = "Confirm a booking",
            description = "Allows a user to confirm an existing booking."
    )
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<Booking> confirmBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
        );
    }
}
