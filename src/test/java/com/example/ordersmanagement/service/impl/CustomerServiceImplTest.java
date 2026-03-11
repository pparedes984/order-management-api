package com.example.ordersmanagement.service.impl;

import com.example.ordersmanagement.dto.customer.CustomerRequest;
import com.example.ordersmanagement.dto.customer.CustomerResponse;
import com.example.ordersmanagement.dto.enums.CustomerStatus;
import com.example.ordersmanagement.entity.Customer;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.mapper.CustomerMapper;
import com.example.ordersmanagement.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .identification("1234567890")
                .name("John Doe")
                .email("john@example.com")
                .phone("555-0100")
                .address("123 Main St")
                .status(CustomerStatus.ACTIVE)
                .build();

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
    void saveCustomerSuccessfully() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerMapper.toEntity(any(CustomerRequest.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);

        CustomerResponse result = customerService.saveCustomer(customerRequest);

        assertNotNull(result);
        assertEquals(customerResponse.getIdentification(), result.getIdentification());
        assertEquals(customerResponse.getName(), result.getName());
        verify(customerRepository).existsByIdentification(customerRequest.getIdentification());
        verify(customerRepository).existsByEmail(customerRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void saveCustomerThrowsExceptionWhenIdentificationExists() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.saveCustomer(customerRequest));

        assertEquals("CUSTOMER_ALREADY_EXISTS", exception.getErrorCode());
        verify(customerRepository).existsByIdentification(customerRequest.getIdentification());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void saveCustomerThrowsExceptionWhenEmailExists() {
        when(customerRepository.existsByIdentification(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.saveCustomer(customerRequest));

        assertEquals("CUSTOMER_EMAIL_ALREADY_EXISTS", exception.getErrorCode());
        verify(customerRepository).existsByIdentification(customerRequest.getIdentification());
        verify(customerRepository).existsByEmail(customerRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerByIdentificationSuccessfully() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);

        CustomerResponse result = customerService.getCustomerByIdentification("1234567890");

        assertNotNull(result);
        assertEquals(customerResponse.getIdentification(), result.getIdentification());
        verify(customerRepository).findByIdentification("1234567890");
    }

    @Test
    void getCustomerByIdentificationThrowsExceptionWhenNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerByIdentification("1234567890"));

        assertEquals("CUSTOMER_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
    }

    @Test
    void getAllCustomersSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> customerPage = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);

        Page<CustomerResponse> result = customerService.getAllCustomers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository).findAll(pageable);
    }

    @Test
    void updateCustomerSuccessfully() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);
        doNothing().when(customerMapper).updateEntity(any(Customer.class), any(CustomerRequest.class));

        CustomerResponse result = customerService.updateCustomer("1234567890", customerRequest);

        assertNotNull(result);
        assertEquals(customerResponse.getIdentification(), result.getIdentification());
        verify(customerRepository).findByIdentification("1234567890");
        verify(customerMapper).updateEntity(customer, customerRequest);
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomerThrowsExceptionWhenNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateCustomer("1234567890", customerRequest));

        assertEquals("CUSTOMER_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomerThrowsExceptionWhenCustomerIsInactive() {
        customer.setStatus(CustomerStatus.INACTIVE);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.updateCustomer("1234567890", customerRequest));

        assertEquals("CUSTOMER_INACTIVE", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deactivateCustomerSuccessfully() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        customerService.deactivateCustomer("1234567890");

        verify(customerRepository).findByIdentification("1234567890");
        verify(customerRepository).save(customer);
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());
    }

    @Test
    void deactivateCustomerThrowsExceptionWhenNotFound() {
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.deactivateCustomer("1234567890"));

        assertEquals("CUSTOMER_NOT_FOUND", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deactivateCustomerThrowsExceptionWhenAlreadyInactive() {
        customer.setStatus(CustomerStatus.INACTIVE);
        when(customerRepository.findByIdentification(anyString())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.deactivateCustomer("1234567890"));

        assertEquals("CUSTOMER_ALREADY_INACTIVE", exception.getErrorCode());
        verify(customerRepository).findByIdentification("1234567890");
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
