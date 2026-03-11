package com.example.ordersmanagement.exception;

/**
 * Exception thrown when an entity is in an invalid state for the requested operation.
 * 
 * This exception is used to represent state conflicts or transition violations
 * (e.g., attempting to cancel an already cancelled order). It is typically caught
 * by the global exception handler and returns an HTTP 409 Conflict response.
 * 
 * @author Paul Paredes
 * @version 1.0
 */
public class InvalidStateException extends RuntimeException {
    private final String errorCode;
    
    /**
     * Constructs an InvalidStateException with a message and error code.
     * 
     * @param message human-readable error message
     * @param errorCode machine-readable error code (e.g., INVALID_ORDER_STATUS)
     */
    public InvalidStateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs an InvalidStateException with only a message.
     * Defaults to a generic error code.
     * 
     * @param message human-readable error message
     */
    public InvalidStateException(String message) {
        super(message);
        this.errorCode = "INVALID_STATE";
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
