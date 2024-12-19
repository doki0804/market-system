package com.marketsystem.api.v1.product.entity;

import com.marketsystem.api.v1.common.entity.BaseEntity;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private long price;
    private int stock;

    @Builder
    public Product(String name, long price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void increaseStock(int amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAMETER);
        }
        this.stock += amount;
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