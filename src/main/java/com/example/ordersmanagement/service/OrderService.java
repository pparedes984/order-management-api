package com.example.ordersmanagement.service;

import com.example.ordersmanagement.dto.order.OrderRequest;
import com.example.ordersmanagement.dto.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract for order management operations.
 * Defines use cases for creating, retrieving, listing, and cancelling
 * customer orders.
 *
 * @author Paul Paredes
 * @version 1.0
 */
public interface OrderService {

    /**
     * Retrieves orders with pagination support.
     *
     * @param pageable pagination and sorting configuration
     * @return paginated order response
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Retrieves an order by order number.
     *
     * @param orderNumber unique order number
     * @return order in response format
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Retrieves all orders associated with a customer identification.
     *
     * @param identification unique customer identification
     * @return list of orders for the customer
     */
    List<OrderResponse> getOrdersByCustomerIdentification(String identification);

    /**
     * Creates a new order.
     *
     * @param request order request with customer and items
     * @return persisted order in response format
     */
    OrderResponse saveOrder(OrderRequest request);

    /**
     * Cancels an existing order.
     *
     * @param orderNumber unique order number
     * @return cancelled order in response format
     */
    OrderResponse cancelOrder(String orderNumber);
}