package com.marketsystem.api.v1.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.marketsystem.api.v1.customer.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndName(Long id, String name);
}