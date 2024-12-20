package com.marketsystem.api.v1.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderCreateResponseDto {
    private Long customerId;
    private String customerName;
    private Long orderId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String transactionId;
    private String status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalPrice;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<OrderItemResponseDto> orderItems;
}