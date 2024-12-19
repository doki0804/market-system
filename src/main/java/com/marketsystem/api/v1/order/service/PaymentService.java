package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.order.controller.PaymentClient;
import com.marketsystem.api.v1.order.dto.PaymentRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentResponseDto requestPayment(Orders order) {
        PaymentRequestDto paymentRequest = new PaymentRequestDto(order.getId().toString(), order.getTotalAmount());
        return paymentClient.requestPayment(paymentRequest);
    }
}
