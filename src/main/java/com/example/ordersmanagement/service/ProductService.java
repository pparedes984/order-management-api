package com.example.ordersmanagement.service;

import com.example.ordersmanagement.dto.product.ProductRequest;
import com.example.ordersmanagement.dto.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for product management operations.
 * Defines use cases for creating, retrieving, updating, listing, and
 * deactivating products in the inventory.
 *
 * @author Paul Paredes
 * @version 1.0
 */
public interface ProductService {

    /**
     * Retrieves products with pagination support.
     *
     * @param pageable pagination and sorting configuration
     * @return paginated product response
     */
    Page<ProductResponse> getAllProducts(Pageable pageable);

    /**
     * Retrieves a product by id.
     *
     * @param id product identifier
     * @return product in response format
     */
    ProductResponse getProductById(Long id);

    /**
     * Creates a new product.
     *
     * @param request product data to persist
     * @return persisted product in response format
     */
    ProductResponse saveProduct(ProductRequest request);

    /**
     * Updates an existing product.
     *
     * @param id product identifier
     * @param request new product data
     * @return updated product in response format
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Deactivates a product by id.
     *
     * @param id product identifier
     */
    void deactivateProduct(Long id);
}