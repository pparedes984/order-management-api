package com.example.ordersmanagement.service.impl;

import com.example.ordersmanagement.dto.customer.CustomerRequest;
import com.example.ordersmanagement.dto.customer.CustomerResponse;
import com.example.ordersmanagement.dto.enums.CustomerStatus;
import com.example.ordersmanagement.entity.Customer;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.mapper.CustomerMapper;
import com.example.ordersmanagement.repository.CustomerRepository;
import com.example.ordersmanagement.service.CustomerService;
import com.example.ordersmanagement.util.CommonUtil;
import com.example.ordersmanagement.util.ServiceMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of the customer management service.
 * 
 * This class is responsible for:
 * - Registering new customers with validation of unique identification and email
 * - Retrieving customer information by identification
 * - Listing customers with pagination support
 * - Updating existing customer data
 * - Deactivating customers (soft delete)
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    /**
     * Creates a new customer in the system.
     * 
     * This method performs the following steps:
     * <ol>
     *   <li>Validates that the identification is not already registered</li>
     *   <li>Validates that the email is not associated with another customer</li>
     *   <li>Maps the request data to a persistence entity</li>
     *   <li>Persists the customer to the database</li>
     *   <li>Returns the created customer in response format</li>
     * </ol>
     *
     * @param request customer data including name, identification, email and phone
     * @return {@code CustomerResponse} the created customer with complete information
     * @throws BusinessException if the identification or email already exist in the system
     */
    @Override
    public CustomerResponse saveCustomer(CustomerRequest request) {
        String method = CommonUtil.getCurrentMethodName();
        
        validateCustomerDoesNotExistByIdentification(request.getIdentification(), method);
        validateCustomerDoesNotExistByEmail(request.getEmail(), method);
        
        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("{} - Customer created with identification: {}", method, savedCustomer.getIdentification());
        
        return customerMapper.toResponse(savedCustomer);
    }

    /**
     * Retrieves a specific customer by their identification number.
     * 
     * @param identification unique customer identification number (ID, passport, etc.)
     * @return {@code CustomerResponse} complete customer data
     * @throws ResourceNotFoundException if no customer with the given identification exists
     */
    @Override
    public CustomerResponse getCustomerByIdentification(String identification) {
        String method = CommonUtil.getCurrentMethodName();
        Customer customer = findCustomerOrThrow(identification, method);
        log.info("{} - Customer found with identification: {}", method, customer.getIdentification());
        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves all customers with pagination support.
     * 
     * Returns a page of active and inactive customers ordered according to the
     * specified pagination configuration.
     *
     * @param pageable pagination configuration including page number, size and sorting
     * @return {@code Page<CustomerResponse>} page of customers mapped to response format
     */
    @Override
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        String method = CommonUtil.getCurrentMethodName();
        Page<Customer> customers = customerRepository.findAll(pageable);
        log.info("{} - Customers found: {}", method, customers.getTotalElements());
        return customers.map(customerMapper::toResponse);
    }

    /**
     * Updates an existing customer's data by their identification.
     * 
     * This method performs the following steps:
     * <ol>
     *   <li>Searches for the customer by their identification</li>
     *   <li>Validates that the customer is not inactive</li>
     *   <li>Updates the customer data with the request values</li>
     *   <li>Persists the changes to the database</li>
     *   <li>Returns the updated customer</li>
     * </ol>
     *
     * @param identification unique customer identifier to update
     * @param request new customer data (name, email, phone, etc.)
     * @return {@code CustomerResponse} updated customer with new data
     * @throws ResourceNotFoundException if no customer with the given identification exists
     * @throws BusinessException if the customer is in INACTIVE status
     */
    @Override
    public CustomerResponse updateCustomer(String identification, CustomerRequest request) {
        String method = CommonUtil.getCurrentMethodName();
        Customer customer = findCustomerOrThrow(identification, method);
        
        validateCustomerIsActive(customer, method);
        customerMapper.updateEntity(customer, request);
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("{} - Customer updated with identification: {}", method, updatedCustomer.getIdentification());
        
        return customerMapper.toResponse(updatedCustomer);
    }

    /**
     * Deactivates (changes status to INACTIVE) a customer by their identification.
     * 
     * This method implements a soft delete, keeping the record in the database
     * but marking it as inactive. An inactive customer cannot be modified nor
     * can perform new orders.
     *
     * @param identification unique customer identifier to deactivate
     * @throws ResourceNotFoundException if no customer with the given identification exists
     * @throws BusinessException if the customer is already in INACTIVE status
     */
    @Override
    public void deactivateCustomer(String identification) {
        String method = CommonUtil.getCurrentMethodName();
        Customer customer = findCustomerOrThrow(identification, method);
        
        validateCustomerIsNotAlreadyInactive(customer, method);
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
        
        log.info("{} - Customer deactivated with identification: {}", method, identification);
    }

    private Customer findCustomerOrThrow(String identification, String method) {
        return customerRepository.findByIdentification(identification)
                .orElseThrow(() -> {
                    log.error("{} - Customer not found with identification: {}", method, identification);
                    return new ResourceNotFoundException(
                        String.format(ServiceMessages.CUSTOMER_NOT_FOUND, identification),
                        "CUSTOMER_NOT_FOUND"
                    );
                });
    }

    /**
     * Validates that no customer exists with the provided identification.
     * 
     * @param identification identification to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if a customer with this identification already exists
     */
    private void validateCustomerDoesNotExistByIdentification(String identification, String method) {
        if (customerRepository.existsByIdentification(identification)) {
            log.error("{} - Customer already exists with identification: {}", method, identification);
            throw new BusinessException(
                String.format(ServiceMessages.CUSTOMER_ALREADY_EXISTS, identification),
                "CUSTOMER_ALREADY_EXISTS"
            );
        }
    }

    /**
     * Validates that no customer exists with the provided email.
     * 
     * @param email email to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if a customer with this email already exists
     */
    private void validateCustomerDoesNotExistByEmail(String email, String method) {
        if (customerRepository.existsByEmail(email)) {
            log.error("{} - Customer already exists with email: {}", method, email);
            throw new BusinessException(
                String.format(ServiceMessages.CUSTOMER_EMAIL_ALREADY_EXISTS, email),
                "CUSTOMER_EMAIL_ALREADY_EXISTS"
            );
        }
    }

    /**
     * Validates that the customer is in ACTIVE status.
     * 
     * @param customer customer to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the customer is INACTIVE
     */
    private void validateCustomerIsActive(Customer customer, String method) {
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            log.error("{} - Customer is inactive: {}", method, customer.getIdentification());
            throw new BusinessException(
                ServiceMessages.CUSTOMER_INACTIVE,
                "CUSTOMER_INACTIVE"
            );
        }
    }

    /**
     * Validates that the customer is not already in INACTIVE status.
     * 
     * @param customer customer to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the customer is already INACTIVE
     */
    private void validateCustomerIsNotAlreadyInactive(Customer customer, String method) {
        if (customer.getStatus() == CustomerStatus.INACTIVE) {
            log.error("{} - Customer is already inactive: {}", method, customer.getIdentification());
            throw new BusinessException(
                ServiceMessages.CUSTOMER_ALREADY_INACTIVE,
                "CUSTOMER_ALREADY_INACTIVE"
            );
        }
    }
}

