package com.booknplay.booking.exception;

import com.booknplay.booking.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request) {
        return buildResponse(
                ex, HttpStatus.NOT_FOUND, request);
    }
    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBooking(
            InvalidBookingException ex,
            HttpServletRequest request) {
        return buildResponse(
                ex, HttpStatus.BAD_REQUEST, request);
    }
    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<ErrorResponse> handleSlotConflict(
            SlotAlreadyBookedException ex,
            HttpServletRequest request) {
        return buildResponse(
                ex, HttpStatus.CONFLICT, request);
    }
    private ResponseEntity<ErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
