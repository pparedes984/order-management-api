package com.example.ordersmanagement.repository;

import com.example.ordersmanagement.entity.Customer;
import com.example.ordersmanagement.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomer(Customer customer);

    Optional<Order> findTopByOrderByIdDesc();
}