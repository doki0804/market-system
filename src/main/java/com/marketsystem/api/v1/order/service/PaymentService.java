package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.order.dto.PaymentDetailsDto;
import com.marketsystem.api.v1.order.dto.PaymentRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.mapper.OrderMapper;
import com.marketsystem.api.v1.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final String paymentUrl = "https://payment-api.free.beeceptor.com/payment";

    /**
     * 외부 결제 API에 결제 요청을 보내는 메서드.
     * @param order 결제 요청을 할 주문 정보(Orders) 엔터티
     * @return 외부 결제 API로부터 받은 응답(PaymentResponseDto) 객체
     */
    public PaymentResponseDto requestPayment(Orders order) {
        PaymentRequestDto request = new PaymentRequestDto(order.getId().toString(), order.getTotalAmount());
        try {
            ResponseEntity<PaymentResponseDto> response = restTemplate.postForEntity(paymentUrl, request, PaymentResponseDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // HTTP 클라이언트 오류 발생 시, 오류 메시지를 로그에 기록
            logger.error("failed payment api response body : {}", e.getResponseBodyAsString());
            // 결제 실패를 나타내는 PaymentResponseDto 객체 생성 및 반환
            return new PaymentResponseDto("FAILED", null, "something wrong!");
        }
    }

    /**
     * 특정 주문에 대한 결제 상세 정보를 조회하는 메서드.
     * @param orderId 결제 상세 정보를 조회할 주문의 ID
     * @return 조회된 결제 상세 정보(PaymentDetailsDto) 객체
     */
    public PaymentDetailsDto getPaymentDetails(Long orderId) {
        Payment payment = paymentRepository.findByOrderIdWithOrderItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found for Order ID: " + orderId));
        PaymentDetailsDto dto = orderMapper.toPaymentDetailsDto(payment);
        return dto;
    }
}
