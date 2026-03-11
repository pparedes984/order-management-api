package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.ErrorResponse;
import com.example.ordersmanagement.dto.product.ProductRequest;
import com.example.ordersmanagement.dto.product.ProductResponse;
import com.example.ordersmanagement.service.ProductService;
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
 * REST controller for product management operations.
 * 
 * Provides endpoints for:
 * - Retrieving products (paginated)
 * - Getting product by id
 * - Creating new products
 * - Updating existing products
 * - Deactivating products (soft delete)
 * 
 * All endpoints return standardized responses with error codes.
 *
 * @author Paul Paredes
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "APIs for managing products including creation, retrieval, update and deactivation")
public class ProductController {
    private final ProductService productService;

    /**
     * Retrieves a paginated list of all products.
     *
     * @param pageable pagination and sorting parameters (page, size, sort)
     * @return Page of products with their complete information
     */
    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of all products with support for sorting and filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @ParameterObject Pageable pageable) {
        String method = getCurrentMethodName();
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        log.info("{} - retrieved {} products with pageable: {}", method, products.getNumberOfElements(), pageable);
        return products;
    }

    /**
     * Retrieves a specific product by its identifier.
     *
     * @param id product identifier
     * @return Product information
     */
    @Operation(
            summary = "Get product by id",
            description = "Retrieves a product using its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found - Error code: PRODUCT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @Parameter(description = "Product identifier", required = true, example = "1")
            @PathVariable Long id) {
        String method = getCurrentMethodName();
        ProductResponse response = productService.getProductById(id);
        log.info("{} - retrieved product with id: {}", method, id);
        return response;
    }

    /**
     * Creates a new product.
     *
     * @param request product data to create
     * @return Created product information
     */
    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with validation of stock and availability"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Validation or business rule violation. Possible error codes: INVALID_STOCK, VALIDATION_ERROR",
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
    public ProductResponse createProduct(
            @Parameter(description = "Product data to create", required = true)
            @Valid @RequestBody ProductRequest request) {
        String method = getCurrentMethodName();
        log.info("{} - creating product, request received: {}", method, request);
        ProductResponse response = productService.saveProduct(request);
        log.info("{} - product created with id: {}", method, response.getId());
        return response;
    }

    /**
     * Updates an existing product.
     *
     * @param id product identifier
     * @param request updated product data
     * @return Updated product information
     */
    @Operation(
            summary = "Update an existing product",
            description = "Updates a product by id. Only available products can be updated"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Validation or business rule violation. Possible error codes: INVALID_STOCK, PRODUCT_UNAVAILABLE, VALIDATION_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found - Error code: PRODUCT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @Parameter(description = "Product identifier", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody ProductRequest request) {
        String method = getCurrentMethodName();
        log.info("{} - updating product with id: {} and request: {}", method, id, request);
        ProductResponse response = productService.updateProduct(id, request);
        log.info("{} - product updated with id: {}", method, id);
        return response;
    }

    /**
     * Deactivates (soft delete) a product by its identifier.
     *
     * @param id product identifier to deactivate
     */
    @Operation(
            summary = "Deactivate a product",
            description = "Deactivates (soft delete) a product by changing its status to UNAVAILABLE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product already unavailable - Error code: PRODUCT_ALREADY_UNAVAILABLE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found - Error code: PRODUCT_NOT_FOUND",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Error code: INTERNAL_SERVER_ERROR",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateProduct(
            @Parameter(description = "Product identifier", required = true, example = "1")
            @PathVariable Long id) {
        String method = getCurrentMethodName();
        log.info("{} - deactivating product with id: {}", method, id);
        productService.deactivateProduct(id);
        log.info("{} - product deactivated with id: {}", method, id);
    }
}