package com.marketsystem.api.v1.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String orderStatus;
    private long totalAmount;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<OrderItemResponseDto> orderItems;
}