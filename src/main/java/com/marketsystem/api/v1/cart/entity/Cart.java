package com.marketsystem.api.v1.cart.entity;

import com.marketsystem.api.v1.common.entity.BaseEntity;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart")
@Getter
public class Cart extends BaseEntity {

    @EmbeddedId
    private CartId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 상품을 추가할 때 수량 업데이트
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
}