package com.booknplay.booking.controller;

import com.booknplay.booking.dto.AvailableSlotDto;
import com.booknplay.booking.dto.BookingRequest;
import com.booknplay.booking.dto.TurfDto;
import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.service.BookingService;
import com.booknplay.booking.utility.BookingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class BookingControllerTest {

    private MockMvc mockMvc;
    private BookingService bookingService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = standaloneSetup(new BookingController(bookingService))
                .setValidator(validator)
                .build();
    }

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setUserId(1L);
        request.setTurfId(7L);
        request.setBookingDate(LocalDate.of(2026, 11, 20));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        Booking booking = Booking.builder()
                .id(101L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(booking);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.turfId").value(7))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void createBooking_shouldRejectMissingValidationFields() throws Exception {
        String requestJson = """
                {
                  "userId": 1,
                  "turfId": 7,
                  "bookingDate": "2026-11-20",
                  "startTime": "09:00:00"
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUpcomingBookings_shouldReturnUpcomingList() throws Exception {
        Booking booking = Booking.builder()
                .id(201L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.of(2026, 11, 21))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.getUpcomingBookings(1L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/bookings/upcoming/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(201))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void getPastBookings_shouldReturnPastList() throws Exception {
        Booking booking = Booking.builder()
                .id(301L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.of(2026, 11, 10))
                .startTime(LocalTime.of(17, 0))
                .endTime(LocalTime.of(18, 0))
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingService.getPastBookings(1L)).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/bookings/past/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(301))
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    @Test
    void cancelBooking_shouldReturnUpdatedBooking() throws Exception {
        Booking booking = Booking.builder()
                .id(401L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.of(2026, 11, 20))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingService.cancelBooking(401L)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/401/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(401))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void searchTurfs_shouldReturnTurfs() throws Exception {
        TurfDto turf = new TurfDto();
        turf.setId(7L);
        turf.setName("City Arena");
        turf.setCity("Hyderabad");
        turf.setArea("Madhapur");
        turf.setSports("Football");
        turf.setBasePrice(new BigDecimal("500"));
        turf.setOpeningTime(LocalTime.of(6, 0));
        turf.setClosingTime(LocalTime.of(23, 0));

        when(bookingService.searchTurfs("Hyderabad", "Madhapur", "Football",
                LocalDate.of(2026, 11, 20), new BigDecimal("600")))
                .thenReturn(List.of(turf));

        mockMvc.perform(get("/api/bookings/search-turfs")
                        .param("city", "Hyderabad")
                        .param("area", "Madhapur")
                        .param("sport", "Football")
                        .param("bookingDate", "2026-11-20")
                        .param("maxPrice", "600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("City Arena"));
    }

    @Test
    void getAvailableSlots_shouldReturnSlots() throws Exception {
        AvailableSlotDto slot = new AvailableSlotDto(LocalTime.of(9, 0), LocalTime.of(10, 0), true);

        when(bookingService.getAvailableSlots(7L, LocalDate.of(2026, 11, 20)))
                .thenReturn(List.of(slot));

        mockMvc.perform(get("/api/bookings/available-slots")
                        .param("turfId", "7")
                        .param("bookingDate", "2026-11-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].endTime").value("10:00:00"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void confirmBooking_shouldReturnConfirmedBooking() throws Exception {
        Booking booking = Booking.builder()
                .id(501L)
                .userId(1L)
                .turfId(7L)
                .bookingDate(LocalDate.of(2026, 11, 20))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(16, 0))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.confirmBooking(501L)).thenReturn(booking);

        mockMvc.perform(put("/api/bookings/501/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(501))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
