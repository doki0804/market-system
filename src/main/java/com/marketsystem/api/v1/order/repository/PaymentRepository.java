package com.marketsystem.api.v1.order.repository;

import com.marketsystem.api.v1.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.order o JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Payment> findByOrderIdWithOrderItems(@Param("orderId") Long orderId);

}
