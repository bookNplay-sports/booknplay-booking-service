package com.booknplay.booking.client;

import com.booknplay.booking.dto.TurfDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@FeignClient(name = "turf-service")
public interface TurfClient {

    @GetMapping("/api/turfs/{turfId}")
    TurfDto getTurfById(
            @PathVariable("turfId") Long turfId
    );

    @GetMapping("/api/turfs/search")
    List<TurfDto> searchApprovedTurfs(
            @RequestParam("city") String city,
            @RequestParam("area") String area,
            @RequestParam("sport") String sport,
            @RequestParam("bookingDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate bookingDate,
            @RequestParam("maxPrice") BigDecimal maxPrice
    );

    @GetMapping("/api/turfs/{turfId}/price")
    BigDecimal calculatePrice(
            @PathVariable("turfId") Long turfId,
            @RequestParam("bookingDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate bookingDate,
            @RequestParam("startTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime
    );
}
