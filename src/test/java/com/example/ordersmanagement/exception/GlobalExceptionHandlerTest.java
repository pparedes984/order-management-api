package com.example.ordersmanagement.exception;

import com.example.ordersmanagement.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void handleNotFoundReturns404() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found", "RESOURCE_NOT_FOUND");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotFound(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getError());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleBusinessReturns400() {
        BusinessException exception = new BusinessException("Business rule violation", "BUSINESS_ERROR");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusiness(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business rule violation", response.getBody().getError());
        assertEquals("BUSINESS_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleInvalidStateReturns409() {
        InvalidStateException exception = new InvalidStateException("Invalid state", "INVALID_STATE");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidState(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid state", response.getBody().getError());
        assertEquals("INVALID_STATE", response.getBody().getErrorCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleInvalidOrderStatusReturns409() {
        InvalidStateException exception = new InvalidStateException(
                "Only orders in CREATED status can be cancelled",
                "INVALID_ORDER_STATUS"
        );

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidState(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ORDER_STATUS", response.getBody().getErrorCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleValidationReturns400() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "Field error message");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fieldName: Field error message", response.getBody().getError());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleValidationReturns400WhenNoFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().getError());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    void handleInvalidJsonReturns400() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getCause()).thenReturn(null);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidJson(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid or malformed JSON in request body", response.getBody().getError());
        assertEquals("INVALID_JSON_FORMAT", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleInvalidJsonReturns400WithInvalidFormatException() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        InvalidFormatException invalidFormatException = mock(InvalidFormatException.class);
        com.fasterxml.jackson.databind.JsonMappingException.Reference reference = 
            new com.fasterxml.jackson.databind.JsonMappingException.Reference(null, "testField");

        when(exception.getCause()).thenReturn(invalidFormatException);
        when(invalidFormatException.getPath()).thenReturn(List.of(reference));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidJson(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid value for field: testField", response.getBody().getError());
        assertEquals("INVALID_JSON_FORMAT", response.getBody().getErrorCode());
    }

    @Test
    void handleGeneralReturns500() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneral(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please contact support", response.getBody().getError());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleConstraintViolationReturns400() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        violations.add(violation);

        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("propertyName");
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", violations);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("propertyName: must not be null", response.getBody().getError());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    void handleConstraintViolationReturns400WhenNoViolations() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", violations);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolation(exception, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error", response.getBody().getError());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }
}