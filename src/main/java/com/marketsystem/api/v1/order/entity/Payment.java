package com.marketsystem.api.v1.order.entity;

import com.marketsystem.api.v1.common.entity.BaseEntity;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(name = "transaction_id")
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "message")
    private String message;

    @Builder
    public Payment(Long id, Orders order, String transactionId, PaymentStatus status, String message) {
        this.id = id;
        this.order = order;
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
    }

    public void markAsSuccess(String transactionId, String message) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.message = message;
    }

    public void markAsFailed(String message) {
        this.status = PaymentStatus.FAILED;
        this.message = message;
    }
}