package com.marketsystem.api.v1.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartResponseDto {
    private Long customerId;
    private Long productId;
    private Integer quantity;
    private String createdAt;
    private String updatedAt;
}