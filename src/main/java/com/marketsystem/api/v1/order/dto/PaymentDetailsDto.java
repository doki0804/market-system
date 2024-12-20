package com.marketsystem.api.v1.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class PaymentDetailsDto {
    private Long paymentId;
    private String transactionId;
    private String status;
    private String message;
    private LocalDateTime paymentDate;
    private OrderResponseDto order;

}