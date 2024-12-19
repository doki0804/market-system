package com.marketsystem.api.v1.order.repository;

import com.marketsystem.api.v1.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
