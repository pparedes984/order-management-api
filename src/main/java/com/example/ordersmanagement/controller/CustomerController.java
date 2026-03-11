package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.ErrorResponse;
import com.example.ordersmanagement.dto.customer.CustomerRequest;
import com.example.ordersmanagement.dto.customer.CustomerResponse;
import com.example.ordersmanagement.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.example.ordersmanagement.util.CommonUtil.getCurrentMethodName;

/**
 * REST Controller for Customer management operations.
 * 
 * Provides endpoints for:
 * - Retrieving customers (paginated)
 * - Getting customer by identification
 * - Creating new customers
 * - Updating existing customers
 * - Deactivating customers (soft delete)
 * 
 * All endpoints return standardized responses with error codes.
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Management", description = "APIs for managing customers including creation, retrieval, update and deactivation")
public class CustomerController {
    private final CustomerService customerService;

    /**
     * Retrieves a paginated list of all customers.
     * 
     * @param pageable pagination and sorting parameters (page, size, sort)
     * @return Page of customers with their complete information
     */
    @Operation(
            summary = "Get all customers",
            description = "Retrieves a paginated list of all customers (active and inactive) with support for sorting and filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public Page<CustomerResponse> getAllCustomers(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @ParameterObject Pageable pageable) {
        String method = getCurrentMethodName();
        Page<CustomerResponse> customerResponsePage = customerService.getAllCustomers(pageable);
        log.info("{} - retrieved {} customers with pageable: {}", method, customerResponsePage.getNumberOfElements(), pageable);
        return customerResponsePage;
    }

    /**
     * Retrieves a specific customer by their identification number.
     * 
     * @param identification unique customer identification (ID, passport, etc.)
     * @return Customer complete information
     */
    @Operation(
            summary = "Get customer by identification",
            description = "Retrieves a specific customer using their unique identification number"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found - Error code: CUSTOMER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{identification}")
    public CustomerResponse getCustomerById(
            @Parameter(description = "Customer identification number", required = true, example = "1234567890")
            @PathVariable String identification) {
        String method = getCurrentMethodName();
        CustomerResponse customerResponse = customerService.getCustomerByIdentification(identification);
        log.info("{} - retrieved customer with identification: {}", method, identification);
        return customerResponse;
    }

    /**
     * Creates a new customer in the system.
     * 
     * @param customerRequest customer data to create
     * @return Created customer information
     */
    @Operation(
            summary = "Create a new customer",
            description = "Creates a new customer with unique identification and email validation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Validation error or business rule violation. Possible error codes: CUSTOMER_ALREADY_EXISTS, CUSTOMER_EMAIL_ALREADY_EXISTS, VALIDATION_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Parameter(description = "Customer data to create", required = true)
            @Valid @RequestBody CustomerRequest customerRequest) {
        String method = getCurrentMethodName();
        log.info("{} - creating customer, request received: {}", method, customerRequest);
        CustomerResponse customerResponse = customerService.saveCustomer(customerRequest);
        log.info("{} - customer created successfully with identification: {}", method, customerResponse.getIdentification());
        return customerResponse;
    }

    /**
     * Updates an existing customer's information.
     * 
     * @param identification customer unique identification
     * @param customerRequest new customer data
     * @return Updated customer information
     */
    @Operation(
            summary = "Update an existing customer",
            description = "Updates customer information by their identification number. Only active customers can be updated"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Customer is inactive or validation error. Possible error codes: CUSTOMER_INACTIVE, VALIDATION_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found - Error code: CUSTOMER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{identification}")
    public CustomerResponse updateCustomer(
            @Parameter(description = "Customer identification number", required = true, example = "1234567890")
            @PathVariable String identification,
            @Parameter(description = "Updated customer data", required = true)
            @Valid @RequestBody CustomerRequest customerRequest) {
        String method = getCurrentMethodName();
        log.info("{} - updating user with identification: {} and request: {}", method, identification, customerRequest);
        CustomerResponse customerResponse = customerService.updateCustomer(identification, customerRequest);
        log.info("{} - customer updated successfully with identification: {}", method, identification);
        return customerResponse;
    }

    /**
     * Deactivates (soft delete) a customer by their identification.
     * 
     * This operation changes the customer status to INACTIVE but keeps the record in the database.
     * Inactive customers cannot perform new orders or be modified.
     * 
     * @param identification unique customer identification to deactivate
     */
    @Operation(
            summary = "Deactivate a customer",
            description = "Deactivates (soft delete) a customer by changing their status to INACTIVE. The customer record is preserved in the database"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Customer already inactive - Error code: CUSTOMER_ALREADY_INACTIVE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found - Error code: CUSTOMER_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{identification}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(
            @Parameter(description = "Customer identification number", required = true, example = "1234567890")
            @PathVariable String identification) {
        String method = getCurrentMethodName();
        log.info("{} - deactivating customer with identification: {}", method, identification);
        customerService.deactivateCustomer(identification);
        log.info("{} - user deactivated successfully with identification: {}", method, identification);
    }
}
