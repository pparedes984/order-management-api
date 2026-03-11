package com.example.ordersmanagement.service.impl;

import com.example.ordersmanagement.dto.enums.ProductStatus;
import com.example.ordersmanagement.dto.product.ProductRequest;
import com.example.ordersmanagement.dto.product.ProductResponse;
import com.example.ordersmanagement.entity.Product;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.mapper.ProductMapper;
import com.example.ordersmanagement.repository.ProductRepository;
import com.example.ordersmanagement.service.ProductService;
import com.example.ordersmanagement.util.CommonUtil;
import com.example.ordersmanagement.util.ServiceMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of the product management service.
 * 
 * This service provides CRUD operations and business logic for managing products.
 * It handles product creation, updates, retrieval, and deactivation with proper
 * validation and error handling.
 * 
 * Main methods:
 * <ul>
 *   <li>getAllProducts(Pageable): Retrieves a paginated list of products</li>
 *   <li>getProductById(Long): Retrieves a product by its identifier</li>
 *   <li>saveProduct(ProductRequest): Creates a new product</li>
 *   <li>updateProduct(Long, ProductRequest): Updates an existing product</li>
 *   <li>deactivateProduct(Long): Deactivates (marks unavailable) a product</li>
 * </ul>
 * 
 * @author Paul Paredes
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Retrieves a page of existing products.
     *
     * @param pageable pagination and sorting information
     * @return page of products in ProductResponse format
     */
    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        String method = CommonUtil.getCurrentMethodName();
        Page<Product> products = productRepository.findAll(pageable);
        log.info("{} - Products found: {}", method, products.getTotalElements());
        return products.map(productMapper::toResponse);
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id product identifier
     * @return the found product in ProductResponse format
     * @throws ResourceNotFoundException if the product does not exist
     */
    @Override
    public ProductResponse getProductById(Long id) {
        String method = CommonUtil.getCurrentMethodName();
        Product product = findProductOrThrow(id, method);
        log.info("{} - Product found with id: {}", method, product.getId());
        return productMapper.toResponse(product);
    }

    /**
     * Creates a new product from the provided information.
     *
     * @param request product data to create
     * @return the created product in ProductResponse format
     * @throws BusinessException if stock is negative
     */
    @Override
    public ProductResponse saveProduct(ProductRequest request) {
        String method = CommonUtil.getCurrentMethodName();
        
        validateStockIsNotNegative(request.getStock(), method);
        
        Product product = productRepository.save(productMapper.toEntity(request));
        log.info("{} - Product created with name: {}", method, product.getName());
        
        return productMapper.toResponse(product);
    }

    /**
     * Updates an existing product with the provided data.
     *
     * @param id product identifier to update
     * @param request new product data
     * @return the updated product in ProductResponse format
     * @throws ResourceNotFoundException if the product does not exist
     * @throws BusinessException if stock is negative or product is unavailable
     */
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        String method = CommonUtil.getCurrentMethodName();
        Product product = findProductOrThrow(id, method);
        
        validateStockIsNotNegative(request.getStock(), method);
        validateProductIsAvailable(product, method);
        
        productMapper.updateEntity(product, request);
        Product updatedProduct = productRepository.save(product);
        log.info("{} - Product updated with name: {}", method, updatedProduct.getName());
        
        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Deactivates (disables) a product by its identifier.
     *
     * @param id product identifier to deactivate
     * @throws ResourceNotFoundException if the product does not exist
     * @throws BusinessException if the product is already unavailable
     */
    @Override
    public void deactivateProduct(Long id) {
        String method = CommonUtil.getCurrentMethodName();
        Product product = findProductOrThrow(id, method);
        
        validateProductIsNotAlreadyUnavailable(product, method);
        product.setStatus(ProductStatus.UNAVAILABLE);
        productRepository.save(product);
        
        log.info("{} - Product deactivated with id: {}", method, id);
    }

    /**
     * Finds a product by its id or throws an exception if not found.
     * 
     * @param id product id to search for
     * @param method name of the calling method (for logging)
     * @return {@code Product} the found product
     * @throws ResourceNotFoundException if the product does not exist
     */
    private Product findProductOrThrow(Long id, String method) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("{} - Product not found with id: {}", method, id);
                    return new ResourceNotFoundException(
                        String.format(ServiceMessages.PRODUCT_NOT_FOUND, id),
                        "PRODUCT_NOT_FOUND"
                    );
                });
    }

    /**
     * Validates that stock is not negative.
     * 
     * @param stock stock to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if stock is negative
     */
    private void validateStockIsNotNegative(Integer stock, String method) {
        if (stock < 0) {
            log.error("{} - Stock cannot be negative", method);
            throw new BusinessException(
                ServiceMessages.PRODUCT_INVALID_STOCK,
                "INVALID_STOCK"
            );
        }
    }

    /**
     * Validates that the product is available (not UNAVAILABLE).
     * 
     * @param product product to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the product is not available
     */
    private void validateProductIsAvailable(Product product, String method) {
        if (product.getStatus() == ProductStatus.UNAVAILABLE) {
            log.error("{} - Product is unavailable: {}", method, product.getId());
            throw new BusinessException(
                ServiceMessages.PRODUCT_UNAVAILABLE,
                "PRODUCT_UNAVAILABLE"
            );
        }
    }

    /**
     * Validates that the product is not already unavailable.
     * 
     * @param product product to validate
     * @param method name of the calling method (for logging)
     * @throws BusinessException if the product is already UNAVAILABLE
     */
    private void validateProductIsNotAlreadyUnavailable(Product product, String method) {
        if (product.getStatus() == ProductStatus.UNAVAILABLE) {
            log.error("{} - Product is already unavailable: {}", method, product.getId());
            throw new BusinessException(
                ServiceMessages.PRODUCT_ALREADY_UNAVAILABLE,
                "PRODUCT_ALREADY_UNAVAILABLE"
            );
        }
    }
}
