package com.marketsystem.api.v1.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderCreateResponseDto {
    private Long orderId;
    private String status;
}