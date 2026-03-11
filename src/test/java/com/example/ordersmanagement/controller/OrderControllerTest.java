package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.enums.OrderStatus;
import com.example.ordersmanagement.dto.order.OrderRequest;
import com.example.ordersmanagement.dto.order.OrderResponse;
import com.example.ordersmanagement.dto.orderitem.OrderItemRequest;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.InvalidStateException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .build();

        orderRequest = OrderRequest.builder()
                .customerIdentification("1234567890")
                .items(List.of(orderItemRequest))
                .build();

        orderResponse = OrderResponse.builder()
                .orderNumber("ORD-00001")
                .customerId(1L)
                .status(OrderStatus.CREATED)
                .totalAmount(new BigDecimal("1999.98"))
                .build();
    }

    @Test
    void getAllOrdersSuccessfully() throws Exception {
        Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse), PageRequest.of(0, 10), 1);
        when(orderService.getAllOrders(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-00001"))
                .andExpect(jsonPath("$.content[0].customerId").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(orderService).getAllOrders(any());
    }

    @Test
    void getOrderByNumberSuccessfully() throws Exception {
        when(orderService.getOrderByOrderNumber(anyString())).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/{orderNumber}", "ORD-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00001"))
                .andExpect(jsonPath("$.customerId").value(1));

        verify(orderService).getOrderByOrderNumber("ORD-00001");
    }

    @Test
    void getOrderByNumberReturnsNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderService.getOrderByOrderNumber(anyString()))
                .thenThrow(new ResourceNotFoundException("Order not found", "ORDER_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/orders/{orderNumber}", "ORD-00001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));

        verify(orderService).getOrderByOrderNumber("ORD-00001");
    }

    @Test
    void getOrdersByCustomerSuccessfully() throws Exception {
        when(orderService.getOrdersByCustomerIdentification(anyString())).thenReturn(List.of(orderResponse));

        mockMvc.perform(get("/api/v1/orders/customer/{identification}", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-00001"))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(orderService).getOrdersByCustomerIdentification("1234567890");
    }

    @Test
    void getOrdersByCustomerReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(orderService.getOrdersByCustomerIdentification(anyString()))
                .thenThrow(new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/orders/customer/{identification}", "1234567890"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(orderService).getOrdersByCustomerIdentification("1234567890");
    }

    @Test
    void getOrdersByCustomerReturnsBadRequestWhenCustomerIsInactive() throws Exception {
        when(orderService.getOrdersByCustomerIdentification(anyString()))
                .thenThrow(new BusinessException("Customer is inactive", "CUSTOMER_INACTIVE"));

        mockMvc.perform(get("/api/v1/orders/customer/{identification}", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_INACTIVE"));

        verify(orderService).getOrdersByCustomerIdentification("1234567890");
    }

    @Test
    void createOrderSuccessfully() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00001"))
                .andExpect(jsonPath("$.customerId").value(1));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenValidationFails() throws Exception {
        orderRequest.setCustomerIdentification(null);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(orderService, never()).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenItemsAreEmpty() throws Exception {
        orderRequest.setItems(Collections.emptyList());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(orderService, never()).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenOrderIsEmpty() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Order must contain at least one product", "EMPTY_ORDER"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("EMPTY_ORDER"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsNotFoundWhenCustomerNotFound() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenCustomerIsInactive() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Customer is inactive", "CUSTOMER_INACTIVE"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_INACTIVE"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsNotFoundWhenProductNotFound() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found", "PRODUCT_NOT_FOUND"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenProductIsUnavailable() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Product unavailable", "PRODUCT_UNAVAILABLE"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_UNAVAILABLE"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void createOrderReturnsBadRequestWhenInsufficientStock() throws Exception {
        when(orderService.saveOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Insufficient stock", "INSUFFICIENT_STOCK"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_STOCK"));

        verify(orderService).saveOrder(any(OrderRequest.class));
    }

    @Test
    void cancelOrderSuccessfully() throws Exception {
        when(orderService.cancelOrder(anyString())).thenReturn(orderResponse);

        mockMvc.perform(patch("/api/v1/orders/{orderNumber}/cancel", "ORD-00001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-00001"));

        verify(orderService).cancelOrder("ORD-00001");
    }

    @Test
    void cancelOrderReturnsNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderService.cancelOrder(anyString()))
                .thenThrow(new ResourceNotFoundException("Order not found", "ORDER_NOT_FOUND"));

        mockMvc.perform(patch("/api/v1/orders/{orderNumber}/cancel", "ORD-00001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));

        verify(orderService).cancelOrder("ORD-00001");
    }

    @Test
    void cancelOrderReturnsBadRequestWhenOrderStatusIsInvalid() throws Exception {
        when(orderService.cancelOrder(anyString()))
                .thenThrow(new InvalidStateException("Invalid order status", "INVALID_ORDER_STATUS"));

        mockMvc.perform(patch("/api/v1/orders/{orderNumber}/cancel", "ORD-00001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ORDER_STATUS"));

        verify(orderService).cancelOrder("ORD-00001");
    }
}