package com.example.ordersmanagement.service.impl;

import com.example.ordersmanagement.dto.enums.ProductStatus;
import com.example.ordersmanagement.dto.product.ProductRequest;
import com.example.ordersmanagement.dto.product.ProductResponse;
import com.example.ordersmanagement.entity.Product;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.mapper.ProductMapper;
import com.example.ordersmanagement.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .status(ProductStatus.AVAILABLE)
                .build();

        productRequest = ProductRequest.builder()
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .status(ProductStatus.AVAILABLE)
                .build();
    }

    @Test
    void getAllProductsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getProductByIdSuccessfully() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(productResponse.getId(), result.getId());
        assertEquals(productResponse.getName(), result.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductByIdThrowsExceptionWhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1L));

        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        verify(productRepository).findById(1L);
    }

    @Test
    void saveProductSuccessfully() {
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse result = productService.saveProduct(productRequest);

        assertNotNull(result);
        assertEquals(productResponse.getName(), result.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void saveProductThrowsExceptionWhenStockIsNegative() {
        productRequest.setStock(-1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.saveProduct(productRequest));

        assertEquals("INVALID_STOCK", exception.getErrorCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void saveProductSuccessfullyWithZeroStock() {
        productRequest.setStock(0);
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse result = productService.saveProduct(productRequest);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProductSuccessfully() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        doNothing().when(productMapper).updateEntity(any(Product.class), any(ProductRequest.class));

        ProductResponse result = productService.updateProduct(1L, productRequest);

        assertNotNull(result);
        assertEquals(productResponse.getName(), result.getName());
        verify(productRepository).findById(1L);
        verify(productMapper).updateEntity(product, productRequest);
        verify(productRepository).save(product);
    }

    @Test
    void updateProductThrowsExceptionWhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, productRequest));

        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsExceptionWhenStockIsNegative() {
        productRequest.setStock(-1);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(1L, productRequest));

        assertEquals("INVALID_STOCK", exception.getErrorCode());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductThrowsExceptionWhenProductIsUnavailable() {
        product.setStatus(ProductStatus.UNAVAILABLE);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(1L, productRequest));

        assertEquals("PRODUCT_UNAVAILABLE", exception.getErrorCode());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deactivateProductSuccessfully() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deactivateProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
        assertEquals(ProductStatus.UNAVAILABLE, product.getStatus());
    }

    @Test
    void deactivateProductThrowsExceptionWhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.deactivateProduct(1L));

        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deactivateProductThrowsExceptionWhenAlreadyUnavailable() {
        product.setStatus(ProductStatus.UNAVAILABLE);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deactivateProduct(1L));

        assertEquals("PRODUCT_ALREADY_UNAVAILABLE", exception.getErrorCode());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}
