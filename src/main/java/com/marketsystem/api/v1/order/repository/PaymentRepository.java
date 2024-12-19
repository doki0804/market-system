package com.marketsystem.api.v1.order.repository;

import com.marketsystem.api.v1.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
