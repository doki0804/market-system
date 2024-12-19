package com.marketsystem.api.v1.cart.repository;

import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.entity.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, CartId> {
    List<Cart> findByCustomerId(Long customerId);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.customer.id = :customerId")
    List<Cart> findByCustomerIdWithProduct(Long customerId);
}
