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
import com.example.ordersmanagement.service.OrderService;
import com.example.ordersmanagement.util.CommonUtil;
import com.example.ordersmanagement.util.ServiceMessages;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


/**
 * Implementation of the order management service.
 * 
 * This class is responsible for:
 * - Creating and managing purchase orders
 * - Validating customers and available products
 * - Controlling inventory during order creation
 * - Retrieving existing orders
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    // Constants
    private static final String ORDER_PREFIX = "ORD-";
    private static final int ORDER_NUMBER_PADDING = 5;

    /**
     * Retrieves all orders with pagination support.
     * 
     * @param pageable pagination configuration (page, size, sorting)
     * @return {@code Page<OrderResponse>} page of orders
     */
    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        String method = CommonUtil.getCurrentMethodName();
        Page<Order> orders = orderRepository.findAll(pageable);
        log.info("{} - Orders found: {}", method, orders.getTotalElements());
        return orders.map(orderMapper::toResponse);
    }

    /**
     * Retrieves a specific order by its order number.
     * 
     * @param orderNumber unique order number to search for
     * @return {@code OrderResponse} order data
     * @throws ResourceNotFoundException if no order with the given number exists
     */
    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        String method = CommonUtil.getCurrentMethodName();
        Order order = getOrder(orderNumber, method);
        log.info("{} - Order found with number: {}", method, orderNumber);
        return orderMapper.toResponse(order);
    }

    /**
     * Retrieves all orders associated with a customer.
     *
     * @param identification id of customer to search for
     * @return {@code List<OrderResponse>} order data
     * @throws ResourceNotFoundException if no customer with the given id exists
     */
    @Override
    public List<OrderResponse> getOrdersByCustomerIdentification(String identification) {
        String method = CommonUtil.getCurrentMethodName();
        Customer customer = validateAndGetCustomer(identification, method);
        List<Order> orders = orderRepository.findByCustomer(customer);
        log.info("{} - Orders {} found for customer: {}", method, orders.size(), customer.getName());
        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new purchase order with validation and inventory management.
     * 
     * This method performs the following steps:
     * <ol>
     *   <li>Validates that the order contains at least one product</li>
     *   <li>Verifies that the customer exists and is active</li>
     *   <li>Validates the availability of each product</li>
     *   <li>Decrements the stock of sold products</li>
     *   <li>Calculates the total order amount</li>
     *   <li>Persists the order to the database</li>
     * </ol>
     * 
     * @param request order data including customer identification and items
     * @return {@code OrderResponse} created order data with unique order number
     * @throws BusinessException if the order is empty or customer is inactive
     * @throws ResourceNotFoundException if customer or product does not exist
     * @throws BusinessException if product is unavailable or insufficient stock
     */
    @Transactional
    @Override
    public OrderResponse saveOrder(OrderRequest request) {
        String method = CommonUtil.getCurrentMethodName();
        log.info("{} - Creating order for customer: {}", method, request.getCustomerIdentification());
        
        validateOrderRequest(request, method);
        Customer customer = validateAndGetCustomer(request.getCustomerIdentification(), method);
        Order order = createOrder(customer);
        List<OrderItem> orderItems = processOrderItems(request.getItems(), order, method);

        order.setItems(orderItems);
        order.setTotalAmount(calculateTotalAmount(orderItems));
        Order savedOrder = orderRepository.save(order);
        log.info("{} - Order created with number: {}", method, order.getOrderNumber());
        
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Cancels an existing order.
     * 
     * This method changes the order status to cancelled and reverses inventory changes.
     * 
     * @param orderNumber unique order number to cancel
     * @return {@code OrderResponse} cancelled order data
     * @throws ResourceNotFoundException if no order with the given number exists
     * @throws InvalidStateException if the order is not in CREATED status
     */
    @Transactional
    @Override
    public OrderResponse cancelOrder(String orderNumber) {
        String method = CommonUtil.getCurrentMethodName();
        log.info("{} - Canceling order with number: {}", method, orderNumber);

        Order order = getOrder(orderNumber, method);
        
        validateOrderCanBeCancelled(order, method);
        reverseOrderItemsStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        log.info("{} - Order cancelled with number: {}", method, orderNumber);
        return orderMapper.toResponse(updatedOrder);
    }

    private Order getOrder(String orderNumber, String method) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> {
                    log.error("{} - Order not found with number: {}", method, orderNumber);
                    return new ResourceNotFoundException(
                        String.format(ServiceMessages.ORDER_NOT_FOUND, orderNumber),
                        "ORDER_NOT_FOUND"
                    );
                });
    }

    /**
     * Generates a unique and incremental order number based on the latest persisted order.
     * 
     * This avoids DB-specific sequences and works across H2/PostgreSQL/MySQL.
     *
     * @return {@code String} order number with format "ORD-{zero-padded number}"
     */
    private String generateOrderNumber() {
        long nextSequence = orderRepository.findTopByOrderByIdDesc()
                .map(Order::getOrderNumber)
                .map(this::extractOrderSequence)
                .orElse(0L) + 1;

        return ORDER_PREFIX + String.format("%0" + ORDER_NUMBER_PADDING + "d", nextSequence);
    }

    private long extractOrderSequence(String orderNumber) {
        if (orderNumber == null || !orderNumber.startsWith(ORDER_PREFIX)) {
            return 0L;
        }

        try {
            return Long.parseLong(orderNumber.substring(ORDER_PREFIX.length()));
        } catch (NumberFormatException ex) {
            log.warn("Invalid order number format found: {}. Resetting sequence base to 0", orderNumber);
            return 0L;
        }
    }

    /**
     * Validates that the order contains at least one product.
     * 
     * @param request order data to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the order does not contain items
     */
    private void validateOrderRequest(OrderRequest request, String method) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.error("{} - Order must contain at least one product", method);
            throw new BusinessException(
                ServiceMessages.ORDER_MINIMUM_ITEMS,
                "EMPTY_ORDER"
            );
        }
    }

    /**
     * Validates that the customer exists and is active.
     * 
     * @param identification customer identification number
     * @param method name of the calling method (for logging)
     * @return {@code Customer} validated and active customer
     * @throws ResourceNotFoundException if customer does not exist
     * @throws BusinessException if customer is inactive
     */
    private Customer validateAndGetCustomer(String identification, String method) {
        Customer customer = customerRepository.findByIdentification(identification)
                .orElseThrow(() -> {
                    log.error("{} - Customer not found with identification: {}", method, identification);
                    return new ResourceNotFoundException(
                        String.format(ServiceMessages.CUSTOMER_NOT_FOUND, identification),
                        "CUSTOMER_NOT_FOUND"
                    );
                });
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            log.error("{} - Customer is inactive: {}", method, identification);
            throw new BusinessException(
                ServiceMessages.CUSTOMER_INACTIVE,
                "CUSTOMER_INACTIVE"
            );
        }
        return customer;
    }

    /**
     * Creates a new order instance with initial data.
     * 
     * @param customer customer associated with the order
     * @return {@code Order} new order with generated number and assigned customer
     */
    private Order createOrder(Customer customer) {
        return Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .build();
    }

    /**
     * Processes order items by performing validation and inventory adjustments.
     * 
     * For each requested item:
     * <ul>
     *   <li>Validates that the product exists</li>
     *   <li>Validates product availability and stock</li>
     *   <li>Decrements the product stock</li>
     *   <li>Creates the OrderItem record</li>
     * </ul>
     * 
     * @param itemRequests list of items requested in the order
     * @param order order to which the items belong
     * @param method name of the calling method (for logging)
     * @return {@code List<OrderItem>} list of processed items
     * @throws ResourceNotFoundException if any product does not exist
     * @throws BusinessException if any product is unavailable or out of stock
     */
    private List<OrderItem> processOrderItems(List<OrderItemRequest> itemRequests, Order order, String method) {
        return itemRequests.stream()
                .map(itemRequest -> {
                    Product product = validateAndGetProduct(itemRequest.getProductId(), method);
                    validateProductAvailability(product, itemRequest.getQuantity(), method);
                    decrementProductStock(product, itemRequest.getQuantity());
                    return createOrderItem(order, product, itemRequest.getQuantity());
                })
                .toList();
    }

    /**
     * Validates that a product exists in the database.
     * 
     * @param productId unique product identifier
     * @param method name of the calling method (for logging)
     * @return {@code Product} validated product
     * @throws ResourceNotFoundException if product with the given id does not exist
     */
    private Product validateAndGetProduct(Long productId, String method) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("{} - Product not found with id: {}", method, productId);
                    return new ResourceNotFoundException(
                        String.format(ServiceMessages.PRODUCT_NOT_FOUND, productId),
                        "PRODUCT_NOT_FOUND"
                    );
                });
    }

    /**
     * Validates that the product is available and has sufficient stock.
     * 
     * @param product product to validate
     * @param quantity requested quantity
     * @param method name of the calling method (for logging)
     * @throws BusinessException if product is unavailable or stock is insufficient
     */
    private void validateProductAvailability(Product product, Integer quantity, String method) {
        validateProductStatus(product, method);
        validateProductStock(product, quantity, method);
    }

    /**
     * Validates that the product status is available (not UNAVAILABLE).
     * 
     * @param product product to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the product is UNAVAILABLE
     */
    private void validateProductStatus(Product product, String method) {
        if (product.getStatus() == ProductStatus.UNAVAILABLE) {
            log.error("{} - Product is not available: {}", method, product.getName());
            throw new BusinessException(
                String.format(ServiceMessages.PRODUCT_UNAVAILABLE_FOR_ORDER, product.getName()),
                "PRODUCT_UNAVAILABLE"
            );
        }
    }

    /**
     * Validates that the product has sufficient stock.
     * 
     * @param product product to validate
     * @param quantity requested quantity
     * @param method name of the calling method (for logging)
     * @throws BusinessException if stock is insufficient
     */
    private void validateProductStock(Product product, Integer quantity, String method) {
        if (product.getStock() < quantity) {
            log.error("{} - Insufficient stock for product: {} - Available: {}, Requested: {}", 
                method, product.getId(), product.getStock(), quantity);
            throw new BusinessException(
                String.format(
                    ServiceMessages.INSUFFICIENT_STOCK,
                    product.getName(),
                    product.getStock(),
                    quantity
                ),
                "INSUFFICIENT_STOCK"
            );
        }
    }

    /**
     * Creates a new OrderItem instance with subtotal calculation.
     * 
     * @param order order to which the item belongs
     * @param product product of the item
     * @param quantity product quantity
     * @return {@code OrderItem} new order item with calculated subtotal
     */
    private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
        BigDecimal subTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .subTotal(subTotal)
                .build();
    }
    
    /**
     * Calculates the total order amount by summing all item subtotals.
     * 
     * Uses streams and reduce for functional aggregation of subtotals.
     * 
     * @param orderItems list of order items
     * @return {@code BigDecimal} total order amount
     */
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Decrements the stock of a product after purchase.
     * 
     * @param product product whose stock will be decremented
     * @param quantity quantity to decrement
     */
    private void decrementProductStock(Product product, Integer quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock decremented for product: {} - Current stock: {}", product.getId(), product.getStock());
    }

    /**
     * Increments the stock of a product (typically for order cancellation).
     * 
     * @param product product whose stock will be incremented
     * @param quantity quantity to increment
     */
    private void incrementProductStock(Product product, Integer quantity) {
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        log.info("Stock incremented for product: {} - Current stock: {}", product.getId(), product.getStock());
    }

    /**
     * Validates that an order can be cancelled.
     * 
     * An order can only be cancelled if it is in CREATED status.
     *
     * @param order order to validate
     * @param method name of the calling method (for logging)
     * @throws InvalidStateException if the order is not in CREATED status
     */
    private void validateOrderCanBeCancelled(Order order, String method) {
        if (order.getStatus() != OrderStatus.CREATED) {
            log.error("{} - Only orders in CREATED status can be cancelled", method);
            throw new InvalidStateException(
                ServiceMessages.ORDER_INVALID_STATUS_FOR_CANCELLATION,
                "INVALID_ORDER_STATUS"
            );
        }
    }

    /**
     * Reverses the stock of all products in a cancelled order.
     * 
     * Iterates over all order items and increments the stock of each product
     * by the quantity that was ordered.
     *
     * @param order order whose stock will be reversed
     */
    private void reverseOrderItemsStock(Order order) {
        order.getItems().forEach(item -> 
            incrementProductStock(item.getProduct(), item.getQuantity())
        );
        log.info("Stock reversed for {} items in order: {}", order.getItems().size(), order.getOrderNumber());
    }
}