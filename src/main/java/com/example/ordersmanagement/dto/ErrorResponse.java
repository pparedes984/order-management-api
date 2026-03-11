package com.example.ordersmanagement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Standardized error response structure for API responses.
 * 
 * This DTO is used by the global exception handler to return consistent error
 * information to clients, including timestamp, HTTP status, error message,
 * error code, and request path for debugging purposes.
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@Getter
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ErrorResponse {
    /**
     * Timestamp when the error occurred.
     */
    LocalDateTime timestamp;
    
    /**
     * HTTP status code of the error response.
     */
    int status;
    
    /**
     * Human-readable error message describing the issue.
     */
    String error;
    
    /**
     * Machine-readable error code for programmatic handling.
     * Examples: CUSTOMER_NOT_FOUND, PRODUCT_UNAVAILABLE, INSUFFICIENT_STOCK
     */
    String errorCode;
    
    /**
     * API path where the error occurred.
     */
    String path;
}
