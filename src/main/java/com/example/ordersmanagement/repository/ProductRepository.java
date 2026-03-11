package com.example.ordersmanagement.repository;

import com.example.ordersmanagement.dto.enums.ProductStatus;
import com.example.ordersmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);
}
