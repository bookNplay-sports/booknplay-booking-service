Booking Service is a microservice of the BookNPlay Sports Turf Booking Platform. It is responsible for managing turf bookings, including creating bookings, viewing booking details, updating booking status, and cancelling bookings. It handles booking-related operations, checks booking details, and integrates with the Payment Service for payment processing. It also includes a scheduler to automatically cancel bookings that remain in pending payment status beyond the specified time. The service integrates with the Notification Service to send booking confirmation and cancellation emails.


Technology Stack: Java 21, Spring Boot, Spring Data JPA, Spring Security, MySQL, Maven, and Eureka Client.

Configuration: Service Name: booking-service | Port: 8084 | Database: booking_db | Eureka Server: http://localhost:8761

The service is part of the BookNPlay microservices architecture and communicates with other services through REST APIs.
