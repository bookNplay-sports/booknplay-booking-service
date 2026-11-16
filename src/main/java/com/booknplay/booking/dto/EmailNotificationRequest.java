package com.booknplay.booking.dto;

import lombok.Data;

@Data
public class EmailNotificationRequest {
    private String to;
    private String subject;
    private String templateType;
    private String bookingDetails;
}