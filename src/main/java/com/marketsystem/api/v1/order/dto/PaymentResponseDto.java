package com.marketsystem.api.v1.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponseDto {
    private String status;
    private String transactionId;
    private String message;
}
