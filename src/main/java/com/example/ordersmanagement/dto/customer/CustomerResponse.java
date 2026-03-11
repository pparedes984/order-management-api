package com.example.ordersmanagement.dto.customer;

import com.example.ordersmanagement.dto.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Customer response with complete information")
public class CustomerResponse {
    
    @Schema(description = "Unique customer ID", example = "1")
    Long id;
    
    @Schema(description = "Customer identification number", example = "1234567890")
    String identification;
    
    @Schema(description = "Full name of the customer", example = "Juan Pérez")
    String name;
    
    @Schema(description = "Email address", example = "juan.perez@example.com")
    String email;
    
    @Schema(description = "Contact phone number", example = "+57 300 123 4567")
    String phone;
    
    @Schema(description = "Physical address", example = "Calle 123 #45-67, Bogotá")
    String address;
    
    @Schema(description = "Customer status (ACTIVE or INACTIVE)", example = "ACTIVE")
    CustomerStatus status;
    
    @Schema(description = "Date and time when the customer was created", example = "2026-03-06T10:15:30")
    LocalDateTime createdAt;
    
    @Schema(description = "Date and time of last update", example = "2026-03-06T10:20:45")
    LocalDateTime updatedAt;
}
