package com.example.ordersmanagement.dto.order;

import com.example.ordersmanagement.dto.orderitem.OrderItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(name = "OrderRequest", description = "Payload para crear una orden")
public class OrderRequest {

    @NotNull(message = "La identificacion del customer es obligatorio")
    @Schema(description = "Identificacion del cliente asociado a la orden", example = "1")
    String customerIdentification;

    @Valid
    @NotEmpty(message = "La orden debe tener al menos un item")
    @Schema(description = "Items incluidos en la orden")
    List<OrderItemRequest> items;
}
