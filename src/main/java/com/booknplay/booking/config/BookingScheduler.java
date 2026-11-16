package com.booknplay.booking.config;

import com.booknplay.booking.entity.Booking;
import com.booknplay.booking.repository.BookingRepository;
import com.booknplay.booking.utility.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {
    private final BookingRepository bookingRepository;
    @Scheduled(fixedRate = 300000)
    public void cancelExpiredBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings =
                bookingRepository.findByStatusAndCreatedAtBefore(
                        BookingStatus.PENDING_PAYMENT,
                        expiryTime
                );
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
        }
        if (!expiredBookings.isEmpty()) {
            bookingRepository.saveAll(expiredBookings);
            log.info("Automatically cancelled {} expired bookings",
                    expiredBookings.size());
        }
    }
}
