package com.marketsystem.api.v1.product.repository;

import com.marketsystem.api.v1.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdIn(Set<Long> ids);
    Page<Product> findByStockGreaterThanEqual(int stock, Pageable pageable);
}