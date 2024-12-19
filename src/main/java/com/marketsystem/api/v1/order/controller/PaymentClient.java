package com.marketsystem.api.v1.order.controller;

import com.marketsystem.api.v1.order.dto.PaymentRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class PaymentClient {
    private final RestTemplate restTemplate = new RestTemplate();

    private final String paymentUrl = "https://payment-api.free.beeceptor.com/payment";

    public PaymentResponseDto requestPayment(PaymentRequestDto request) {
        ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(paymentUrl, request, PaymentResponseDto.class);
        return response.getBody();
    }
}