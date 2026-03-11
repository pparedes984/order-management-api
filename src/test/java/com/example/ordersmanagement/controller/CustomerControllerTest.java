package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.customer.CustomerRequest;
import com.example.ordersmanagement.dto.customer.CustomerResponse;
import com.example.ordersmanagement.dto.enums.CustomerStatus;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customerRequest = CustomerRequest.builder()
                .identification("1234567890")
                .name("John Doe")
                .email("john@example.com")
                .phone("555-0100")
                .address("123 Main St")
                .build();

        customerResponse = CustomerResponse.builder()
                .id(1L)
                .identification("1234567890")
                .name("John Doe")
                .email("john@example.com")
                .phone("555-0100")
                .address("123 Main St")
                .status(CustomerStatus.ACTIVE)
                .build();
    }

    @Test
    void getAllCustomersSuccessfully() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(List.of(customerResponse), PageRequest.of(0, 10), 1);
        when(customerService.getAllCustomers(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].identification").value("1234567890"))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(customerService).getAllCustomers(any());
    }

    @Test
    void getCustomerByIdSuccessfully() throws Exception {
        when(customerService.getCustomerByIdentification(anyString())).thenReturn(customerResponse);

        mockMvc.perform(get("/api/v1/customers/{identification}", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(customerService).getCustomerByIdentification("1234567890");
    }

    @Test
    void getCustomerByIdReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerService.getCustomerByIdentification(anyString()))
                .thenThrow(new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/customers/{identification}", "1234567890"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(customerService).getCustomerByIdentification("1234567890");
    }

    @Test
    void createCustomerSuccessfully() throws Exception {
        when(customerService.saveCustomer(any(CustomerRequest.class))).thenReturn(customerResponse);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identification").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService).saveCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomerReturnsBadRequestWhenIdentificationAlreadyExists() throws Exception {
        when(customerService.saveCustomer(any(CustomerRequest.class)))
                .thenThrow(new BusinessException("Customer already exists", "CUSTOMER_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_ALREADY_EXISTS"));

        verify(customerService).saveCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomerReturnsBadRequestWhenEmailAlreadyExists() throws Exception {
        when(customerService.saveCustomer(any(CustomerRequest.class)))
                .thenThrow(new BusinessException("Email already exists", "CUSTOMER_EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_EMAIL_ALREADY_EXISTS"));

        verify(customerService).saveCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomerReturnsBadRequestWhenValidationFails() throws Exception {
        customerRequest.setName("");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(customerService, never()).saveCustomer(any(CustomerRequest.class));
    }

    @Test
    void createCustomerReturnsBadRequestWhenEmailIsInvalid() throws Exception {
        customerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(customerService, never()).saveCustomer(any(CustomerRequest.class));
    }

    @Test
    void updateCustomerSuccessfully() throws Exception {
        when(customerService.updateCustomer(anyString(), any(CustomerRequest.class))).thenReturn(customerResponse);

        mockMvc.perform(put("/api/v1/customers/{identification}", "1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("1234567890"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService).updateCustomer(eq("1234567890"), any(CustomerRequest.class));
    }

    @Test
    void updateCustomerReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerService.updateCustomer(anyString(), any(CustomerRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"));

        mockMvc.perform(put("/api/v1/customers/{identification}", "1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(customerService).updateCustomer(eq("1234567890"), any(CustomerRequest.class));
    }

    @Test
    void updateCustomerReturnsBadRequestWhenCustomerIsInactive() throws Exception {
        when(customerService.updateCustomer(anyString(), any(CustomerRequest.class)))
                .thenThrow(new BusinessException("Customer is inactive", "CUSTOMER_INACTIVE"));

        mockMvc.perform(put("/api/v1/customers/{identification}", "1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_INACTIVE"));

        verify(customerService).updateCustomer(eq("1234567890"), any(CustomerRequest.class));
    }

    @Test
    void deleteCustomerSuccessfully() throws Exception {
        doNothing().when(customerService).deactivateCustomer(anyString());

        mockMvc.perform(patch("/api/v1/customers/{identification}", "1234567890"))
                .andExpect(status().isNoContent());

        verify(customerService).deactivateCustomer("1234567890");
    }

    @Test
    void deleteCustomerReturnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Customer not found", "CUSTOMER_NOT_FOUND"))
                .when(customerService).deactivateCustomer(anyString());

        mockMvc.perform(patch("/api/v1/customers/{identification}", "1234567890"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));

        verify(customerService).deactivateCustomer("1234567890");
    }

    @Test
    void deleteCustomerReturnsBadRequestWhenCustomerAlreadyInactive() throws Exception {
        doThrow(new BusinessException("Customer already inactive", "CUSTOMER_ALREADY_INACTIVE"))
                .when(customerService).deactivateCustomer(anyString());

        mockMvc.perform(patch("/api/v1/customers/{identification}", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_ALREADY_INACTIVE"));

        verify(customerService).deactivateCustomer("1234567890");
    }
}
