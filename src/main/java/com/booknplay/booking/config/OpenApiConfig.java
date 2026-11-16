package com.booknplay.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bookingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BooknPlay Booking Service API")
                        .description("API documentation for Booking Management")
                        .version("1.0.0"));
    }
}
