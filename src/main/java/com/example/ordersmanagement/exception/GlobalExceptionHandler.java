package com.example.ordersmanagement.exception;

import com.example.ordersmanagement.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST API error responses.
 * 
 * This component intercepts all exceptions thrown by the application and
 * converts them into standardized HTTP error responses with consistent
 * error codes, status codes, and error messages. It ensures that clients
 * receive clear, actionable error information regardless of the exception type.
 * 
 * Handles the following exception types:
 * - ResourceNotFoundException (404 Not Found)
 * - BusinessException (400 Bad Request)
 * - InvalidStateException (409 Conflict)
 * - MethodArgumentNotValidException (400 Bad Request)
 * - HttpMessageNotReadableException (400 Bad Request)
 * - Generic Exception (500 Internal Server Error)
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns HTTP 404 Not Found.
     * 
     * @param ex the ResourceNotFoundException thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 404 and error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        String errorCode = ex.getErrorCode();
        log.error("Resource not found: {} - Error Code: {}", ex.getMessage(), errorCode);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(ex.getMessage(), HttpStatus.NOT_FOUND, errorCode, request));
    }

    /**
     * Handles BusinessException and returns HTTP 400 Bad Request.
     * 
     * This handler is used for violations of business logic and rules,
     * such as insufficient stock, inactive customers, or invalid operations.
     * 
     * @param ex the BusinessException thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 400 and error details
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request) {

        String errorCode = ex.getErrorCode();
        log.error("Business rule violation: {} - Error Code: {}", ex.getMessage(), errorCode);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getMessage(), HttpStatus.BAD_REQUEST, errorCode, request));
    }

    /**
     * Handles InvalidStateException and returns HTTP 409 Conflict.
     * 
     * This handler is used for state transition violations, such as
     * attempting to cancel an order in an invalid status.
     * 
     * @param ex the InvalidStateException thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 409 and error details
     */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidStateException ex,
            HttpServletRequest request) {
        
        String errorCode = ex.getErrorCode();
        log.error("Invalid state: {} - Error Code: {}", ex.getMessage(), errorCode);
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(ex.getMessage(), HttpStatus.CONFLICT, errorCode, request));
    }

    /**
     * Handles MethodArgumentNotValidException and returns HTTP 400 Bad Request.
     * 
     * This exception is thrown when request body validation fails (e.g., missing
     * required fields, invalid format, constraint violations).
     * 
     * @param ex the MethodArgumentNotValidException thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 400 and validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.error("Validation error: {}", message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", request));
    }

    /**
     * Handles HttpMessageNotReadableException and returns HTTP 400 Bad Request.
     * 
     * This exception is thrown when the request body is malformed JSON or
     * contains invalid field values.
     * 
     * @param ex the HttpMessageNotReadableException thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 400 and error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = "Invalid or malformed JSON in request body";

        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            message = "Invalid value for field: " +
                    invalidFormat.getPath().get(0).getFieldName();
        }

        log.error("Invalid JSON: {}", message);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(message, HttpStatus.BAD_REQUEST, "INVALID_JSON_FORMAT", request));
    }

    /**
     * Handles all uncaught exceptions and returns HTTP 500 Internal Server Error.
     * 
     * This is the fallback handler for unexpected exceptions that are not
     * explicitly handled by other handlers.
     * 
     * @param ex the exception thrown
     * @param request the HTTP request that caused the exception
     * @return ResponseEntity with HTTP 500 and generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError("An unexpected error occurred. Please contact support", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", request));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Validation error");

        log.error("Constraint violation: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", request));
    }

    /**
     * Builds a standardized ErrorResponse object.
     * 
     * @param message human-readable error message
     * @param status HTTP status code
     * @param errorCode machine-readable error code
     * @param request the HTTP request
     * @return ErrorResponse object with all details populated
     */
    private ErrorResponse buildError(String message, HttpStatus status, String errorCode, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(message)
                .errorCode(errorCode)
                .path(request.getRequestURI())
                .build();
    }
}
