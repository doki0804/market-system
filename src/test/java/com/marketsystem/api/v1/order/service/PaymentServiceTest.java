package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.dto.PaymentDetailsDto;
import com.marketsystem.api.v1.order.dto.PaymentRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import com.marketsystem.api.v1.order.mapper.OrderMapper;
import com.marketsystem.api.v1.order.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<PaymentRequestDto> paymentRequestCaptor;

    private final String paymentUrl = "https://payment-api.free.beeceptor.com/payment";

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 결제 요청이 성공적으로 처리되고, 성공 응답을 반환하는 경우를 테스트합니다.
     */
    @Test
    void requestPayment_SuccessResponse() {
        // Given
        Orders order = Orders.builder()
                .id(1L)
                .totalAmount(10000L)
                .build();

        PaymentResponseDto mockResponse = new PaymentResponseDto("SUCCESS", "TX12345", "Payment processed successfully");
        ResponseEntity<PaymentResponseDto> responseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.postForEntity(eq(paymentUrl), any(PaymentRequestDto.class), eq(PaymentResponseDto.class)))
                .thenReturn(responseEntity);

        // When
        PaymentResponseDto paymentResponse = paymentService.requestPayment(order);

        // Then
        assertNotNull(paymentResponse);
        assertEquals("SUCCESS", paymentResponse.getStatus());
        assertEquals("TX12345", paymentResponse.getTransactionId());
        assertEquals("Payment processed successfully", paymentResponse.getMessage());

        verify(restTemplate, times(1)).postForEntity(eq(paymentUrl), paymentRequestCaptor.capture(), eq(PaymentResponseDto.class));
        PaymentRequestDto capturedRequest = paymentRequestCaptor.getValue();
        assertEquals(order.getId().toString(), capturedRequest.getOrderId());
        assertEquals(order.getTotalAmount(), capturedRequest.getNumber());
    }

    /**
     * 결제 요청 시 외부 결제 API 호출이 실패하고, 실패 응답을 반환하는 경우를 테스트합니다.
     */
    @Test
    void requestPayment_paymentApiFailure() {
        // Given
        Orders order = Orders.builder()
                .id(2L)
                .totalAmount(5000L)
                .build();

        when(restTemplate.postForEntity(eq(paymentUrl), any(PaymentRequestDto.class), eq(PaymentResponseDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden"));

        // When
        PaymentResponseDto paymentResponse = paymentService.requestPayment(order);

        // Then
        assertNotNull(paymentResponse);
        assertEquals("FAILED", paymentResponse.getStatus());
        assertNull(paymentResponse.getTransactionId());
        assertEquals("something wrong!", paymentResponse.getMessage());

        verify(restTemplate, times(1)).postForEntity(eq(paymentUrl), paymentRequestCaptor.capture(), eq(PaymentResponseDto.class));
        PaymentRequestDto capturedRequest = paymentRequestCaptor.getValue();
        assertEquals(order.getId().toString(), capturedRequest.getOrderId());
        assertEquals(order.getTotalAmount(), capturedRequest.getNumber());
    }

    /**
     * 결제 상세 정보를 정상적으로 조회하는 경우를 테스트합니다.
     */
    @Test
    void getPaymentDetails_paymentExists() {
        // Given
        Long orderId = 1L;
        Payment payment = Payment.builder()
                .id(100L)
                .order(Orders.builder().id(orderId).build())
                .transactionId("TX67890")
                .status(PaymentStatus.SUCCESS)
                .message("Payment processed successfully")
                .build();

        OrderResponseDto orderResponseDto = OrderResponseDto.builder()
                .orderId(orderId)
                .orderStatus("PAID")
                .totalAmount(10000L)
                .orderItems(null) // 필요한 경우 추가적으로 설정
                .build();

        PaymentDetailsDto dto = PaymentDetailsDto.builder()
                .paymentId(payment.getId())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus().toString())
                .message(payment.getMessage())
                .paymentDate(LocalDateTime.now())
                .order(orderResponseDto)
                .build();

        when(paymentRepository.findByOrderIdWithOrderItems(orderId)).thenReturn(Optional.of(payment));
        when(orderMapper.toPaymentDetailsDto(payment)).thenReturn(dto);

        // When
        PaymentDetailsDto result = paymentService.getPaymentDetails(orderId);

        // Then
        assertNotNull(result);
        assertEquals(payment.getId(), result.getPaymentId());
        assertEquals(payment.getTransactionId(), result.getTransactionId());
        assertEquals(payment.getStatus().toString(), result.getStatus());
        assertEquals(payment.getMessage(), result.getMessage());
        assertNotNull(result.getPaymentDate());
        assertNotNull(result.getOrder());
        assertEquals(orderId, result.getOrder().getOrderId());
        assertEquals("PAID", result.getOrder().getOrderStatus());
        assertEquals(10000L, result.getOrder().getTotalAmount());

        verify(paymentRepository, times(1)).findByOrderIdWithOrderItems(orderId);
        verify(orderMapper, times(1)).toPaymentDetailsDto(payment);
    }

    @Test
    void getPaymentDetails_paymentNotFound() {
        // Given
        Long orderId = 2L;

        when(paymentRepository.findByOrderIdWithOrderItems(orderId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            paymentService.getPaymentDetails(orderId);
        });

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        assertEquals("Payment not found for Order ID: " + orderId, exception.getMessage());

        verify(paymentRepository, times(1)).findByOrderIdWithOrderItems(orderId);
        verify(orderMapper, never()).toPaymentDetailsDto(any());
    }
}