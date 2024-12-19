package com.marketsystem.api.v1.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String orderStatus;
    private long totalAmount;
    private List<OrderItemResponseDto> orderItems;
}