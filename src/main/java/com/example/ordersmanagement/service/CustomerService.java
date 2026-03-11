package com.example.ordersmanagement.service;

import com.example.ordersmanagement.dto.customer.CustomerRequest;
import com.example.ordersmanagement.dto.customer.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for customer management operations.
 * Defines use cases for creating, retrieving, updating, listing, and
 * deactivating customers in the system.
 *
 * @author Paul Paredes
 * @version 1.0
 */
public interface CustomerService {
    /**
     * Saves a new customer in the system.
     *
     * @param request the customer data to be saved
     * @return the saved customer data
     */
    CustomerResponse saveCustomer(CustomerRequest request);

    /**
     * Retrieves a customer by their identification number.
     *
     * @param identification the identification number of the customer
     * @return the customer data
     */
    CustomerResponse getCustomerByIdentification(String identification);

    /**
     * Retrieves all customers in a paginated format.
     *
     * @param pageable pagination information
     * @return a page of customer data
     */
    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    /**
     * Updates an existing customer's data.
     *
     * @param identification the identification number of the customer
     * @param request the new customer data
     * @return the updated customer data
     */
    CustomerResponse updateCustomer(String identification, CustomerRequest request);

    /**
     * Deactivates a customer by their identification number.
     *
     * @param identification the identification number of the customer
     */
    void deactivateCustomer(String identification);
}