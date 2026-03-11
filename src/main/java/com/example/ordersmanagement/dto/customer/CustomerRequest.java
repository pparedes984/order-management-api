package com.example.ordersmanagement.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(description = "Customer creation/update request")
public class CustomerRequest {

    @NotBlank(message = "La identificación es obligatoria")
    @Schema(description = "Unique customer identification number (ID, passport, tax ID, etc.)", 
            example = "1234567890", 
            required = true)
    String identification;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Schema(description = "Full name of the customer", 
            example = "Juan Pérez", 
            required = true,
            maxLength = 100)
    String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Schema(description = "Email address for contact and notifications", 
            example = "juan.perez@example.com", 
            required = true)
    String email;

    @Schema(description = "Contact phone number", 
            example = "+57 300 123 4567")
    String phone;

    @Schema(description = "Physical address or domicile", 
            example = "Calle 123 #45-67, Bogotá")
    String address;
}
