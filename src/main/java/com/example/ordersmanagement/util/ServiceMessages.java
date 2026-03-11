package com.example.ordersmanagement.util;

/**
 * Central repository for all service layer messages and error descriptions.
 * 
 * This class contains all constants used in service implementations for:
 * - Exception messages
 * - Validation error messages
 * - Log messages
 * 
 * By centralizing messages, we ensure consistency across the application
 * and facilitate future internationalization (i18n) if needed.
 *
 * @author Portfolio
 * @version 1.0
 */
public class ServiceMessages {

    // ============ CUSTOMER MESSAGES ============
    public static final String CUSTOMER_NOT_FOUND = "Customer not found with identification: %s";
    public static final String CUSTOMER_ALREADY_EXISTS = "Customer already exists with identification: %s";
    public static final String CUSTOMER_EMAIL_ALREADY_EXISTS = "Customer already exists with email: %s";
    public static final String CUSTOMER_INACTIVE = "Cannot modify an inactive customer";
    public static final String CUSTOMER_ALREADY_INACTIVE = "Customer is already inactive";

    // ============ PRODUCT MESSAGES ============
    public static final String PRODUCT_NOT_FOUND = "Product not found with id: %s";
    public static final String PRODUCT_INVALID_STOCK = "Stock cannot be negative";
    public static final String PRODUCT_UNAVAILABLE = "Cannot modify an unavailable product";
    public static final String PRODUCT_ALREADY_UNAVAILABLE = "Product is already unavailable";

    // ============ ORDER MESSAGES ============
    public static final String ORDER_NOT_FOUND = "Order not found with number: %s";
    public static final String ORDER_MINIMUM_ITEMS = "Order must contain at least one product";
    public static final String ORDER_INVALID_STATUS_FOR_CANCELLATION = "Only orders in CREATED status can be cancelled";
    
    // ============ ORDER ITEM MESSAGES ============
    public static final String PRODUCT_UNAVAILABLE_FOR_ORDER = "Product is not available: %s";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock for product: %s. Available: %d, Requested: %d";

    // ============ LOG MESSAGES ============
    public static final String LOG_CUSTOMER_CREATED = "Customer created with identification: {}";
    public static final String LOG_CUSTOMER_FOUND = "Customer found with identification: {}";
    public static final String LOG_CUSTOMERS_FOUND = "Customers found: {}";
    public static final String LOG_CUSTOMER_UPDATED = "Customer updated with identification: {}";
    public static final String LOG_CUSTOMER_DEACTIVATED = "Customer deactivated with identification: {}";
    
    public static final String LOG_PRODUCT_CREATED = "Product created with name: {}";
    public static final String LOG_PRODUCT_FOUND = "Product found with id: {}";
    public static final String LOG_PRODUCTS_FOUND = "Products found: {}";
    public static final String LOG_PRODUCT_UPDATED = "Product updated with name: {}";
    public static final String LOG_PRODUCT_DEACTIVATED = "Product deactivated with id: {}";
    
    public static final String LOG_ORDER_CREATED = "Order created with number: {}";
    public static final String LOG_ORDER_FOUND = "Order found with number: {}";
    public static final String LOG_ORDERS_FOUND = "Orders found: {}";
    public static final String LOG_ORDER_CANCELLED = "Order cancelled with number: {}";
    public static final String LOG_ORDER_STOCK_DECREMENTED = "Stock decremented for product: {} - Current stock: {}";
    public static final String LOG_ORDER_STOCK_INCREMENTED = "Stock incremented for product: {} - Current stock: {}";
    public static final String LOG_ORDER_STOCK_REVERSED = "Stock reversed for {} items in order: {}";

    // ============ ERROR LOG MESSAGES ============
    public static final String ERROR_CUSTOMER_NOT_FOUND = "Customer not found with identification: {}";
    public static final String ERROR_CUSTOMER_ALREADY_EXISTS = "Customer already exists with identification: {}";
    public static final String ERROR_CUSTOMER_EMAIL_EXISTS = "Customer already exists with email: {}";
    public static final String ERROR_CUSTOMER_INACTIVE = "Customer is inactive: {}";
    public static final String ERROR_CUSTOMER_ALREADY_INACTIVE = "Customer is already inactive: {}";
    
    public static final String ERROR_PRODUCT_NOT_FOUND = "Product not found with id: {}";
    public static final String ERROR_PRODUCT_INVALID_STOCK = "Stock cannot be negative";
    public static final String ERROR_PRODUCT_UNAVAILABLE = "Product is unavailable: {}";
    public static final String ERROR_PRODUCT_ALREADY_UNAVAILABLE = "Product is already unavailable: {}";
    
    public static final String ERROR_ORDER_NOT_FOUND = "Order not found with number: {}";
    public static final String ERROR_ORDER_MINIMUM_ITEMS = "Order must contain at least one product";
    public static final String ERROR_ORDER_INVALID_STATUS = "Only orders in CREATED status can be cancelled";
    public static final String ERROR_ORDER_STOCK_INSUFFICIENT = "Insufficient stock for product: {} - Available: {}, Requested: {}";
    public static final String ERROR_ORDER_PRODUCT_UNAVAILABLE = "Product is not available: {}";

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class containing only static constants.
     */
    private ServiceMessages() {
        throw new AssertionError("Cannot instantiate ServiceMessages utility class");
    }
}
