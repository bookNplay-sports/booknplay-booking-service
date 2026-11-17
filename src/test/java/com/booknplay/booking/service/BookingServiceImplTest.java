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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TurfClient turfClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private TurfDto validTurf;

    @BeforeEach
    void setUp() {
        validTurf = new TurfDto();
        validTurf.setId(7L);
        validTurf.setOpeningTime(LocalTime.of(8, 0));
        validTurf.setClosingTime(LocalTime.of(22, 0));
    }

    @Test
    void createBooking_shouldCreateBooking_whenRequestIsValid() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        when(turfClient.getTurfById(7L)).thenReturn(validTurf);
        when(bookingRepository.existsOverlappingBooking(
                eq(7L),
                eq(request.getBookingDate()),
                eq(request.getStartTime()),
                eq(request.getEndTime()),
                any(List.class)
        )).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(101L);
            booking.setCreatedAt(LocalDateTime.now());
            return booking;
        });

        Booking booking = bookingService.createBooking(request);

        assertThat(booking.getId()).isEqualTo(101L);
        assertThat(booking.getUserId()).isEqualTo(1L);
        assertThat(booking.getTurfId()).isEqualTo(7L);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(booking.getEndTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void createBooking_shouldRejectWhenEndTimeIsNotAfterStartTime() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(10, 0));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("End time must be after start time");
    }

    @Test
    void createBooking_shouldRejectWhenBookingDurationIsNotExactlyOneHour() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 30));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Booking slot must be exactly 1 hour");
    }

    @Test
    void createBooking_shouldRejectPastBookingDate() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().minusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Booking date cannot be in the past");
    }

    @Test
    void createBooking_shouldRejectWhenSlotIsOutsideOperatingHours() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(7, 30));
        request.setEndTime(LocalTime.of(8, 30));

        when(turfClient.getTurfById(7L)).thenReturn(validTurf);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Booking slot is outside turf operating hours");
    }

    @Test
    void createBooking_shouldRejectWhenSameSlotIsAlreadyBooked() {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        when(turfClient.getTurfById(7L)).thenReturn(validTurf);
        when(bookingRepository.existsOverlappingBooking(
                eq(7L),
                eq(request.getBookingDate()),
                eq(request.getStartTime()),
                eq(request.getEndTime()),
                any(List.class)
        )).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SlotAlreadyBookedException.class)
                .hasMessage("This turf slot is already booked");
    }

    @Test
    void getUpcomingBookings_shouldReturnCurrentAndFutureActiveBookings() {
        LocalDate today = LocalDate.now();
        Booking futureBooking = Booking.builder()
                .id(1L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(today.plusDays(1))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking todayBooking = Booking.builder()
                .id(2L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(today)
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(21, 0))
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(bookingRepository.findByUserIdAndBookingDateAfterAndStatusIn(
                1L, today, List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED)
        )).thenReturn(new ArrayList<>(List.of(futureBooking)));
        when(bookingRepository.findByUserIdAndBookingDateAndStatusIn(
                1L, today, List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED)
        )).thenReturn(new ArrayList<>(List.of(todayBooking)));

        List<Booking> result = bookingService.getUpcomingBookings(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Booking::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getPastBookings_shouldReturnPastBookings() {
        Booking pastBooking = Booking.builder()
                .id(3L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.now().minusDays(2))
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findByUserIdAndBookingDateBefore(1L, LocalDate.now()))
                .thenReturn(List.of(pastBooking));

        List<Booking> result = bookingService.getPastBookings(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
    }

    @Test
    void cancelBooking_shouldUpdateBookingStatus() {
        Booking booking = Booking.builder()
                .id(50L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.now().plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking cancelled = bookingService.cancelBooking(50L);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_shouldThrowWhenBookingDoesNotExist() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(99L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found");
    }

    @Test
    void confirmBooking_shouldUpdatePendingBookingToConfirmed() {
        Booking booking = Booking.builder()
                .id(20L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        Booking confirmed = bookingService.confirmBooking(20L);

        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_shouldRejectIfBookingIsNotPendingPayment() {
        Booking booking = Booking.builder()
                .id(21L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(21L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(21L))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Only pending payment bookings can be confirmed");
    }

    @Test
    void getAvailableSlots_shouldReturnOnlyAvailableSlots() {
        LocalDate date = LocalDate.now().plusDays(1);
        validTurf.setOpeningTime(LocalTime.of(9, 0));
        validTurf.setClosingTime(LocalTime.of(12, 0));

        Booking booked = Booking.builder()
                .userId(1L)
                .turfId(7L)
                .bookingDate(date)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(turfClient.getTurfById(7L)).thenReturn(validTurf);
        when(bookingRepository.findByTurfIdAndBookingDateAndStatusIn(
                eq(7L),
                eq(date),
                any(List.class)
        )).thenReturn(List.of(booked));

        List<AvailableSlotDto> slots = bookingService.getAvailableSlots(7L, date);

        assertThat(slots).isNotEmpty();
        assertThat(slots).extracting(AvailableSlotDto::getStartTime)
                .contains(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void searchTurfs_shouldDelegateToTurfClient() {
        LocalDate date = LocalDate.now().plusDays(1);
        TurfDto turf = new TurfDto();
        turf.setId(7L);
        turf.setName("City Arena");
        turf.setBasePrice(new BigDecimal("500"));

        when(turfClient.searchApprovedTurfs("Hyderabad", "Madhapur", "Football", date, new BigDecimal("600")))
                .thenReturn(List.of(turf));

        List<TurfDto> result = bookingService.searchTurfs("Hyderabad", "Madhapur", "Football", date, new BigDecimal("600"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("City Arena");
    }
}
