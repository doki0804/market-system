package com.marketsystem.api.v1.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponseDto {
    private String status;
    private String transactionId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
}
