package com.example.ordersmanagement.dto.product;

import com.example.ordersmanagement.dto.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(name = "ProductResponse", description = "Respuesta con la información del producto")
public class ProductResponse {

    @Schema(description = "ID del producto", example = "1")
    Long id;

    @Schema(description = "Nombre del producto", example = "Teclado mecánico")
    String name;

    @Schema(description = "Descripción del producto", example = "Teclado mecánico switches brown")
    String description;

    @Schema(description = "Precio del producto", example = "79.99")
    BigDecimal price;

    @Schema(description = "Stock disponible", example = "50")
    Integer stock;

    @Schema(description = "Estado del producto", example = "AVAILABLE")
    ProductStatus status;

    @Schema(description = "Fecha de creación", example = "2026-01-30T10:15:30")
    LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2026-01-30T10:16:10")
    LocalDateTime updatedAt;
}
