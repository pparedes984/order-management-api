package com.example.ordersmanagement.repository;

import com.example.ordersmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdentification(String identification);
    Optional<Customer> findByEmail(String email);
    boolean existsByIdentification(String identification);
    boolean existsByEmail(String email);
}
