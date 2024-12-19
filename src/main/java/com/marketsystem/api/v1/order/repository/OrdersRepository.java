package com.marketsystem.api.v1.order.repository;

import com.marketsystem.api.v1.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
