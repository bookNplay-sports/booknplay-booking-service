package com.booknplay.booking.config;

import com.booknplay.booking.client.UserClient;
import com.booknplay.booking.client.NotificationClient;
import com.booknplay.booking.dto.EmailNotificationRequest;
import com.booknplay.booking.dto.UserDto;
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
public class BookingReminderScheduler {
    private final BookingRepository bookingRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    @Scheduled(fixedRate = 10000)
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderLimit = now.plusHours(24);
        List<Booking> bookings =
                bookingRepository
                        .findByStatusAndReminderSentFalseAndBookingDateBetween(
                                BookingStatus.CONFIRMED,
                                now.toLocalDate(),
                                reminderLimit.toLocalDate()
                        );
        for (Booking booking : bookings) {
            LocalDateTime bookingStart =
                    LocalDateTime.of(
                            booking.getBookingDate(),
                            booking.getStartTime()
                    );
            if (bookingStart.isAfter(now)
                    && !bookingStart.isAfter(reminderLimit)) {
                try {
                    UserDto user = userClient.getUserById(
                            booking.getUserId()
                    );
                    EmailNotificationRequest request =
                            new EmailNotificationRequest();
                    request.setTo(user.getEmail());
                    request.setSubject("BooknPlay Booking Reminder");
                    request.setTemplateType("BOOKING_REMINDER");
                    request.setBookingDetails(
                            "Turf ID: " + booking.getTurfId()
                                    + "\nDate: " + booking.getBookingDate()
                                    + "\nTime: " + booking.getStartTime()
                    );
                    notificationClient.sendEmail(request);
                    booking.setReminderSent(true);
                    bookingRepository.save(booking);
                    log.info("Reminder sent for booking ID: {}",
                            booking.getId());
                } catch (Exception ex) {
                    log.error("Failed to send reminder for booking ID: {}",
                            booking.getId(), ex);
                }
            }
        }
    }
}
