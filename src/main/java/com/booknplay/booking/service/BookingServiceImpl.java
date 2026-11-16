package com.booknplay.booking.service;


import com.booknplay.booking.client.TurfClient;
import com.booknplay.booking.dto.AvailableSlotDto;
import com.booknplay.booking.dto.BookingRequest;
import com.booknplay.booking.dto.TurfDto;
import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.exception.BookingNotFoundException;
import com.booknplay.booking.exception.InvalidBookingException;
import com.booknplay.booking.exception.SlotAlreadyBookedException;
import com.booknplay.booking.repository.BookingRepository;
import com.booknplay.booking.utility.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TurfClient turfClient;

    @Override
    public Booking createBooking(BookingRequest request) {

        // Validate end time

        if (!request.getEndTime().isAfter(request.getStartTime())) {

            throw new InvalidBookingException(

                    "End time must be after start time"

            );

        }

        // Booking must be exactly 1 hour

        if (!request.getEndTime().equals(request.getStartTime().plusHours(1))) {

            throw new InvalidBookingException(

                    "Booking slot must be exactly 1 hour"

            );

        }

        // Validate booking date

        if (request.getBookingDate().isBefore(LocalDate.now())) {

            throw new InvalidBookingException(

                    "Booking date cannot be in the past"

            );

        }

        // Fetch turf details

        TurfDto turf = turfClient.getTurfById(request.getTurfId());

        // Validate turf operating hours

        if (request.getStartTime().isBefore(turf.getOpeningTime())

                || request.getEndTime().isAfter(turf.getClosingTime())) {

            throw new InvalidBookingException(

                    "Booking slot is outside turf operating hours"

            );

        }

        // Check overlapping bookings

        boolean alreadyBooked = bookingRepository.existsOverlappingBooking(

                request.getTurfId(),

                request.getBookingDate(),

                request.getStartTime(),

                request.getEndTime(),

                List.of(

                        BookingStatus.PENDING_PAYMENT,

                        BookingStatus.CONFIRMED

                )

        );

        if (alreadyBooked) {

            throw new SlotAlreadyBookedException(

                    "This turf slot is already booked"

            );

        }

        // Create booking

        Booking booking = Booking.builder()

                .userId(request.getUserId())

                .turfId(request.getTurfId())

                .bookingDate(request.getBookingDate())

                .startTime(request.getStartTime())

                .endTime(request.getEndTime())

                .status(BookingStatus.PENDING_PAYMENT)

                .createdAt(LocalDateTime.now())

                .build();

        return bookingRepository.save(booking);

    }

    @Override
    public List<Booking> getUpcomingBookings(Long userId) {
        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING_PAYMENT,
                BookingStatus.CONFIRMED
        );
        List<Booking> bookings =
                bookingRepository.findByUserIdAndBookingDateAfterAndStatusIn(
                        userId,
                        LocalDate.now(),
                        activeStatuses
                );
        bookings.addAll(
                bookingRepository.findByUserIdAndBookingDateAndStatusIn(
                        userId,
                        LocalDate.now(),
                        activeStatuses
                )
        );
        return bookings;
    }
    @Override
    public List<Booking> getPastBookings(Long userId) {
        return bookingRepository.findByUserIdAndBookingDateBefore(
                userId, java.time.LocalDate.now());
    }
    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException("Booking not found"));
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Override
    public List<TurfDto> searchTurfs(String city, String area, String sport, LocalDate bookingDate, BigDecimal maxPrice
    ) {
        return turfClient.searchApprovedTurfs(
                city, area, sport, bookingDate, maxPrice
        );
    }


    @Override
    public List<AvailableSlotDto> getAvailableSlots(Long turfId, LocalDate bookingDate) {
        TurfDto turf = turfClient.getTurfById(turfId);
        List<Booking> bookings = bookingRepository.findByTurfIdAndBookingDateAndStatusIn(turfId, bookingDate, List.of(
                                BookingStatus.PENDING_PAYMENT,
                                BookingStatus.CONFIRMED));
        List<AvailableSlotDto> slots = new ArrayList<>();
        LocalTime slotStart = turf.getOpeningTime();
        LocalTime closingTime = turf.getClosingTime();
        while (!slotStart.plusHours(1).isAfter(closingTime)) {
            LocalTime slotEnd = slotStart.plusHours(1);
            // Create an effectively final variable for lambda
            LocalTime currentSlotStart = slotStart;
            boolean available = bookings.stream().noneMatch(booking ->
                    currentSlotStart.isBefore(booking.getEndTime())
                            && slotEnd.isAfter(booking.getStartTime())
            );
            if (available) {slots.add(new AvailableSlotDto(slotStart, slotEnd, true));}
            slotStart = slotEnd;
            if (slots.size() == 10) {
                break;
            }
        }
        return slots;
    }
    @Override
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException("Booking not found with id: " + bookingId));
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingException("Only pending payment bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

}

