package com.marketsystem.api.v1.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartRequestDto {
    private Long customerId;
    private Long productId;
    private Integer quantity;
}
