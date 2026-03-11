package com.example.ordersmanagement.controller;

import com.example.ordersmanagement.dto.enums.ProductStatus;
import com.example.ordersmanagement.dto.product.ProductRequest;
import com.example.ordersmanagement.dto.product.ProductResponse;
import com.example.ordersmanagement.exception.BusinessException;
import com.example.ordersmanagement.exception.ResourceNotFoundException;
import com.example.ordersmanagement.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
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
    void getAllProductsSuccessfully() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(productResponse), PageRequest.of(0, 10), 1);
        when(productService.getAllProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getAllProducts(any());
    }

    @Test
    void getProductByIdSuccessfully() throws Exception {
        when(productService.getProductById(anyLong())).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductByIdReturnsNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Product not found", "PRODUCT_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/products/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(productService).getProductById(1L);
    }

    @Test
    void createProductSuccessfully() throws Exception {
        when(productService.saveProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService).saveProduct(any(ProductRequest.class));
    }

    @Test
    void createProductReturnsBadRequestWhenValidationFails() throws Exception {
        productRequest.setName("");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(productService, never()).saveProduct(any(ProductRequest.class));
    }

    @Test
    void createProductReturnsBadRequestWhenPriceIsNegative() throws Exception {
        productRequest.setPrice(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(productService, never()).saveProduct(any(ProductRequest.class));
    }

    @Test
    void createProductReturnsBadRequestWhenStockIsNegative() throws Exception {
        when(productService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BusinessException("Invalid stock", "INVALID_STOCK"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STOCK"));

        verify(productService).saveProduct(any(ProductRequest.class));
    }

    @Test
    void updateProductSuccessfully() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void updateProductReturnsNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found", "PRODUCT_NOT_FOUND"));

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void updateProductReturnsBadRequestWhenProductIsUnavailable() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new BusinessException("Product unavailable", "PRODUCT_UNAVAILABLE"));

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_UNAVAILABLE"));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void updateProductReturnsBadRequestWhenStockIsInvalid() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new BusinessException("Invalid stock", "INVALID_STOCK"));

        mockMvc.perform(put("/api/v1/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STOCK"));

        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void deactivateProductSuccessfully() throws Exception {
        doNothing().when(productService).deactivateProduct(anyLong());

        mockMvc.perform(patch("/api/v1/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService).deactivateProduct(1L);
    }

    @Test
    void deactivateProductReturnsNotFoundWhenProductDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found", "PRODUCT_NOT_FOUND"))
                .when(productService).deactivateProduct(anyLong());

        mockMvc.perform(patch("/api/v1/products/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));

        verify(productService).deactivateProduct(1L);
    }

    @Test
    void deactivateProductReturnsBadRequestWhenProductAlreadyUnavailable() throws Exception {
        doThrow(new BusinessException("Product already unavailable", "PRODUCT_ALREADY_UNAVAILABLE"))
                .when(productService).deactivateProduct(anyLong());

        mockMvc.perform(patch("/api/v1/products/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_ALREADY_UNAVAILABLE"));

        verify(productService).deactivateProduct(1L);
    }
}
