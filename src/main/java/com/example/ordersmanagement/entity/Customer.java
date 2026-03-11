package com.example.ordersmanagement.entity;

import com.example.ordersmanagement.dto.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false,unique = true)
    String identification;

    @Column(nullable = false, length = 100)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    String phone;

    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CustomerStatus status;

    @OneToMany(mappedBy = "customer")
    List<Order> orders;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = CustomerStatus.ACTIVE;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
