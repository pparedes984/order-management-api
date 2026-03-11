package com.example.ordersmanagement.mapper;

import com.example.ordersmanagement.dto.orderitem.OrderItemResponse;
import com.example.ordersmanagement.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItemResponse toResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .subTotal(item.getSubTotal())
                .build();
    }
}
