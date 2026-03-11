package com.example.ordersmanagement.mapper;

import com.example.ordersmanagement.dto.order.OrderResponse;
import com.example.ordersmanagement.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(
                        order.getItems()
                                .stream()
                                .map(orderItemMapper::toResponse)
                                .collect(Collectors.toList())
                )
                .build();
    }
}
