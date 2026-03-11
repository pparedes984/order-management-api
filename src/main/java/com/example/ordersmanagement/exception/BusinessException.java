package com.example.ordersmanagement.exception;

/**
 * Exception thrown when business logic validation fails.
 * 
 * This exception represents violations of business rules and constraints
 * (e.g., insufficient stock, inactive customer, invalid order state).
 * It is typically caught by the global exception handler and returns
 * an HTTP 400 Bad Request response.
 * 
 * @author Paul Paredes
 * @version 1.0
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    /**
     * Constructs a BusinessException with a message and error code.
     * 
     * @param message human-readable error message
     * @param errorCode machine-readable error code (e.g., INSUFFICIENT_STOCK)
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a BusinessException with only a message.
     * Defaults to a generic error code.
     * 
     * @param message human-readable error message
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    /**
     * Returns the machine-readable error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
