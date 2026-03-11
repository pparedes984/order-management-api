package com.example.ordersmanagement.dto.order;

import com.example.ordersmanagement.dto.enums.OrderStatus;
import com.example.ordersmanagement.dto.orderitem.OrderItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(name = "OrderResponse", description = "Respuesta con la información de una orden")
public class OrderResponse {

    @Schema(description = "Número único de orden", example = "ORD-20260130-0001")
    String orderNumber;

    @Schema(description = "ID del cliente", example = "1")
    Long customerId;

    @Schema(description = "Estado actual de la orden", example = "CREATED")
    OrderStatus status;

    @Schema(description = "Total de la orden", example = "159.98")
    BigDecimal totalAmount;

    @Schema(description = "Fecha de creación", example = "2026-01-30T10:15:30")
    LocalDateTime createdAt;

    @Schema(description = "Fecha de última actualización", example = "2026-01-30T10:16:10")
    LocalDateTime updatedAt;

    @Schema(description = "Items de la orden")
    List<OrderItemResponse> items;
}
