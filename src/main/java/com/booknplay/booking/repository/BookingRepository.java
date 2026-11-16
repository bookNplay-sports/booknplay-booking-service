package com.booknplay.booking.repository;

import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.utility.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
       SELECT COUNT(b) > 0 FROM Booking b
       WHERE b.turfId = :turfId
       AND b.bookingDate = :bookingDate
       AND b.status IN :statuses
       AND b.startTime < :endTime
       AND b.endTime > :startTime
   """)
    boolean existsOverlappingBooking(
            @Param("turfId") Long turfId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<BookingStatus> statuses
    );
    List<Booking> findByUserIdAndBookingDateAfterAndStatusIn(
            Long userId,
            LocalDate date,
            List<BookingStatus> statuses
    );
    List<Booking> findByUserIdAndBookingDateAndStatusIn(
            Long userId,
            LocalDate date,
            List<BookingStatus> statuses
    );


    List<Booking> findByStatusAndCreatedAtBefore(
            BookingStatus status,
            LocalDateTime dateTime
    );
    List<Booking> findByStatusAndReminderSentFalseAndBookingDateBetween(
            BookingStatus status,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Booking> findByTurfIdAndBookingDateAndStatusIn(
            Long turfId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );
    List<Booking> findByUserIdAndBookingDateAfter(
            Long userId, LocalDate date);
    List<Booking> findByUserIdAndBookingDateBefore(
            Long userId, LocalDate date);
    List<Booking> findByUserIdAndBookingDate(
            Long userId, LocalDate date);
}