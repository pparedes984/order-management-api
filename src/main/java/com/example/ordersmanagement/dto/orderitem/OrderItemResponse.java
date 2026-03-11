package com.example.ordersmanagement.dto.orderitem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Schema(name = "OrderItemResponse", description = "Detalle de un item de orden")
public class OrderItemResponse {

    @Schema(description = "ID del producto", example = "10")
    Long productId;

    @Schema(description = "Nombre del producto", example = "Teclado mecánico")
    String productName;

    @Schema(description = "Cantidad comprada", example = "2")
    Integer quantity;

    @Schema(description = "Precio del producto al momento de la compra", example = "79.99")
    BigDecimal priceAtPurchase;

    @Schema(description = "Subtotal (precio * cantidad)", example = "159.98")
    BigDecimal subTotal;
}

