package com.marketsystem.api.v1.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long productId;
    private String productName;
    private long productPrice;
    private int quantity;
}