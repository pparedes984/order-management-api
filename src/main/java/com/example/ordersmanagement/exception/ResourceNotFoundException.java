package com.example.ordersmanagement.exception;

/**
 * Exception thrown when a requested resource is not found in the database.
 * 
 * This exception is typically caught by the global exception handler and
 * returns an HTTP 404 Not Found response with appropriate error details.
 * 
 * @author Paul Paredes
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode;
    
    /**
     * Constructs a ResourceNotFoundException with a message and error code.
     * 
     * @param message human-readable error message
     * @param errorCode machine-readable error code (e.g., CUSTOMER_NOT_FOUND)
     */
    public ResourceNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a ResourceNotFoundException with only a message.
     * Defaults to a generic error code.
     * 
     * @param message human-readable error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
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
