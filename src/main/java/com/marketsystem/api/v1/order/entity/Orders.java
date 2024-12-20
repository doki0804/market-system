package com.marketsystem.api.v1.order.entity;

import com.marketsystem.api.v1.common.entity.BaseEntity;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Orders(Customer customer, Long totalAmount, OrderStatus status, List<OrderItem> orderItems) {
        this.customer = customer;
        this.totalAmount = totalAmount;
        this.status = status;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                addOrderItem(item);
            }
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.associateWithOrder(this);
    }

    // 주문 항목 제거 메서드
    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.dissociateWithOrder();
    }

    public void markAsPaid() {
        this.status = OrderStatus.PAID;
    }

    public void markAsFailed() {
        this.status = OrderStatus.FAILED;
    }

}