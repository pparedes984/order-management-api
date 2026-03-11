package com.example.ordersmanagement.service.impl;

import com.example.ordersmanagement.dto.enums.CustomerStatus;
import com.example.ordersmanagement.dto.enums.OrderStatus;
import com.example.ordersmanagement.dto.enums.ProductStatus;
import com.example.ordersmanagement.dto.order.OrderRequest;
import com.example.ordersmanagement.dto.order.OrderResponse;
import com.example.ordersmanagement.dto.orderitem.OrderItemRequest;
import com.example.ordersmanagement.entity.Customer;
import com.example.ordersmanagement.entity.Order;
import com.example.ordersmanagement.entity.OrderItem;
import com.example.ordersmanagement.entity.Product;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.InvalidStateException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.mapper.OrderMapper;
import com.example.ordersmanagement.repository.CustomerRepository;
import com.example.ordersmanagement.repository.OrderRepository;
import com.example.ordersmanagement.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderRequest orderRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .identification("1234567890")
                .name("John Doe")
                .email("john@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .status(ProductStatus.AVAILABLE)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .priceAtPurchase(new BigDecimal("999.99"))
                .subTotal(new BigDecimal("1999.98"))
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-00001")
                .customer(customer)
                .status(OrderStatus.CREATED)
                .totalAmount(new BigDecimal("1999.98"))
                .items(new ArrayList<>(List.of(orderItem)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderItem.setOrder(order);

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
    void getAllOrdersSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getOrderByOrderNumberSuccessfully() {
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderByOrderNumber("ORD-00001");

        assertNotNull(result);
        assertEquals(orderResponse.getOrderNumber(), result.getOrderNumber());
        verify(orderRepository).findByOrderNumber("ORD-00001");
    }

    @Test
    void getOrderByOrderNumberThrowsExceptionWhenNotFound() {
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderByOrderNumber("ORD-00001"));

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        verify(orderRepository).findByOrderNumber("ORD-00001");
    }

    @Test
    void getOrdersByCustomerIdentificationSuccessfully() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(any(Customer.class))).thenReturn(List.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByCustomerIdentification("1234567890");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(customerRepository).findByIdentification("1234567890");
        verify(orderRepository).findByCustomer(customer);
    }

    @Test
    void getOrdersByCustomerIdentificationThrowsExceptionWhenCustomerNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrdersByCustomerIdentification("1234567890"));

        assertEquals("CUSTOMER_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(orderRepository, never()).findByCustomer(any(Customer.class));
    }

    @Test
    void getOrdersByCustomerIdentificationThrowsExceptionWhenCustomerInactive() {
        customer.setStatus(CustomerStatus.INACTIVE);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.getOrdersByCustomerIdentification("1234567890"));

        assertEquals("CUSTOMER_INACTIVE", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(orderRepository, never()).findByCustomer(any(Customer.class));
    }

    @Test
    void saveOrderSuccessfully() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.saveOrder(orderRequest);

        assertNotNull(result);
        assertEquals(orderResponse.getOrderNumber(), result.getOrderNumber());
        verify(customerRepository).findByIdentification("1234567890");
        verify(productRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(productRepository).save(product);
    }

    @Test
    void saveOrderThrowsExceptionWhenOrderIsEmpty() {
        orderRequest.setItems(Collections.emptyList());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("EMPTY_ORDER", exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenItemsIsNull() {
        orderRequest.setItems(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("EMPTY_ORDER", exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenCustomerNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("CUSTOMER_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenCustomerIsInactive() {
        customer.setStatus(CustomerStatus.INACTIVE);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("CUSTOMER_INACTIVE", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenProductNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenProductIsUnavailable() {
        product.setStatus(ProductStatus.UNAVAILABLE);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("PRODUCT_UNAVAILABLE", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrderThrowsExceptionWhenInsufficientStock() {
        product.setStock(1);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.saveOrder(orderRequest));

        assertEquals("INSUFFICIENT_STOCK", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderSuccessfully() {
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.cancelOrder("ORD-00001");

        assertNotNull(result);
        verify(orderRepository).findByOrderNumber("ORD-00001");
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void cancelOrderThrowsExceptionWhenNotFound() {
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.cancelOrder("ORD-00001"));

        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        verify(orderRepository).findByOrderNumber("ORD-00001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderThrowsExceptionWhenOrderStatusIsNotCreated() {
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));

        InvalidStateException exception = assertThrows(InvalidStateException.class,
                () -> orderService.cancelOrder("ORD-00001"));

        assertEquals("INVALID_ORDER_STATUS", exception.getErrorCode());
        verify(orderRepository).findByOrderNumber("ORD-00001");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderRevertsStockSuccessfully() {
        int initialStock = product.getStock();
        when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            assertEquals(initialStock + 2, savedProduct.getStock());
            return savedProduct;
        });
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        orderService.cancelOrder("ORD-00001");

        verify(productRepository, atLeastOnce()).save(any(Product.class));
    }
}