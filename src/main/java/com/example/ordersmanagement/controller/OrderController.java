package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.ErrorResponse;
import com.example.ordersmanagement.dto.order.OrderRequest;
import com.example.ordersmanagement.dto.order.OrderResponse;
import com.example.ordersmanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.ordersmanagement.util.CommonUtil.getCurrentMethodName;

/**
 * REST controller for order management operations.
 * 
 * Provides endpoints for:
 * - Retrieving orders (paginated)
 * - Getting order by order number
 * - Listing orders by customer identification
 * - Creating new orders
 * - Cancelling existing orders
 * 
 * All endpoints return standardized responses with error codes.
 *
 * @author Paul Paredes
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/orders")
@Tag(name = "Order Management", description = "APIs for creating, retrieving, and cancelling orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Retrieves a paginated list of all orders.
     *
     * @param pageable pagination and sorting parameters (page, size, sort)
     * @return Page of orders with their complete information
     */
    @Operation(
            summary = "Get all orders",
            description = "Retrieves a paginated list of all orders with support for sorting and filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public Page<OrderResponse> getAllOrders(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @ParameterObject Pageable pageable) {
        String method = getCurrentMethodName();
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        log.info("{} - retrieved {} orders with pageable: {}", method, orders.getNumberOfElements(), pageable);
        return orders;
    }

    /**
     * Retrieves a specific order by its order number.
     *
     * @param orderNumber unique order number
     * @return Order information
     */
    @Operation(
            summary = "Get order by number",
            description = "Retrieves an order using its unique order number"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found - Error code: ORDER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{orderNumber}")
    public OrderResponse getOrderByNumber(
            @Parameter(description = "Order number", required = true, example = "ORD-00001")
            @PathVariable String orderNumber) {
        String method = getCurrentMethodName();
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        log.info("{} - retrieved order with number: {}", method, orderNumber);
        return response;
    }

    /**
     * Retrieves all orders associated with a customer.
     *
     * @param identification customer identification
     * @return List of orders for the given customer
     */
    @Operation(
            summary = "Get orders by customer",
            description = "Retrieves all orders associated with a customer identification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Customer is inactive. Error code: CUSTOMER_INACTIVE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found - Error code: CUSTOMER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/customer/{identification}")
    public List<OrderResponse> getOrdersByCustomer(
            @Parameter(description = "Customer identification", required = true, example = "1234567890")
            @PathVariable String identification) {
        String method = getCurrentMethodName();
        List<OrderResponse> orders = orderService.getOrdersByCustomerIdentification(identification);
        log.info("{} - retrieved {} orders for customer: {}", method, orders.size(), identification);
        return orders;
    }

    /**
     * Creates a new order.
     *
     * @param orderRequest order data including customer identification and items
     * @return Created order information
     */
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with validation of customer and product availability"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Validation or business rule violation. Possible error codes: EMPTY_ORDER, CUSTOMER_INACTIVE, PRODUCT_UNAVAILABLE, INSUFFICIENT_STOCK, VALIDATION_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found - Possible error codes: CUSTOMER_NOT_FOUND, PRODUCT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Parameter(description = "Order data to create", required = true)
            @Valid @RequestBody OrderRequest orderRequest) {
        String method = getCurrentMethodName();
        log.info("{} - creating order, received request {} for customer: {}", method, orderRequest, orderRequest.getCustomerIdentification());
        OrderResponse response = orderService.saveOrder(orderRequest);
        log.info("{} - order created with number: {}", method, response.getOrderNumber());
        return response;
    }

    /**
     * Cancels an existing order by its order number.
     *
     * @param orderNumber order number to cancel
     * @return Cancelled order information
     */
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an existing order if it is in CREATED status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Invalid order status. Error code: INVALID_ORDER_STATUS",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found - Error code: ORDER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{orderNumber}/cancel")
    public OrderResponse cancelOrder(
            @Parameter(description = "Order number", required = true, example = "ORD-00001")
            @PathVariable String orderNumber) {
        String method = getCurrentMethodName();
        log.info("{} - cancelling order with number: {}", method, orderNumber);
        OrderResponse response = orderService.cancelOrder(orderNumber);
        log.info("{} - order cancelled with number: {}", method, orderNumber);
        return response;
    }
}