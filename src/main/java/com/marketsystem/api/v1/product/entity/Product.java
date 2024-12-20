package com.marketsystem.api.v1.product.entity;

import com.marketsystem.api.v1.common.entity.BaseEntity;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "description", length = 1000)
    private String description;
    private long price;
    private int stock;

    @Builder
    public Product(Long id, String name, String description, long price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAMETER);
        }
        if (this.stock < amount) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        this.stock -= amount;
    }
}