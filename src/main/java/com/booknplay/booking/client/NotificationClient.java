package com.booknplay.booking.client;


import com.booknplay.booking.dto.EmailNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    @PostMapping("/notify/email")
    void sendEmail(@RequestBody EmailNotificationRequest request);
}