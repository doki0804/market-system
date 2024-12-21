package com.marketsystem.api.v1.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.GlobalExceptionHandler;
import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentDetailsDto;
import com.marketsystem.api.v1.order.service.OrderService;
import com.marketsystem.api.v1.order.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/payment - Success")
    void placeOrder_Success() throws Exception {
        // Given
        OrderRequestDto requestDto = new OrderRequestDto(1L);

        OrderCreateResponseDto responseDto = OrderCreateResponseDto.builder()
                .customerId(1L)
                .customerName("John Doe")
                .orderId(100L)
                .transactionId("TX12345")
                .status("PAID")
                .message("Payment successful")
                .totalPrice(10000L)
                .orderItems(null)
                .build();

        when(orderService.placeOrder(requestDto.getCustomerId())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.SUCCESS.getStatus()))
                .andExpect(jsonPath("$.data.customerId").value(1L))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"))
                .andExpect(jsonPath("$.data.orderId").value(100L))
                .andExpect(jsonPath("$.data.transactionId").value("TX12345"))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.message").value("Payment successful"))
                .andExpect(jsonPath("$.data.totalPrice").value(10000L));

        verify(orderService, times(1)).placeOrder(requestDto.getCustomerId());
    }

    @Test
    @DisplayName("GET /api/v1/payment/{orderId} - Success")
    void getPaymentDetail_Success() throws Exception {
        // Given
        Long orderId = 1L;
        PaymentDetailsDto paymentDetailsDto = PaymentDetailsDto.builder()
                .paymentId(100L)
                .transactionId("TX12345")
                .status("SUCCESS")
                .message("Payment successful")
                .paymentDate(java.time.LocalDateTime.now())
                .order(null)
                .build();

        when(paymentService.getPaymentDetails(orderId)).thenReturn(paymentDetailsDto);

        // When & Then
        mockMvc.perform(get("/api/v1/payment/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.SUCCESS.getStatus()))
                .andExpect(jsonPath("$.data.paymentId").value(100L))
                .andExpect(jsonPath("$.data.transactionId").value("TX12345"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.message").value("Payment successful"))
                .andExpect(jsonPath("$.data.paymentDate").exists());

        verify(paymentService, times(1)).getPaymentDetails(orderId);
    }

    @Test
    @DisplayName("GET /api/v1/payment/{orderId} - Payment Not Found")
    void getPaymentDetail_PaymentNotFound() throws Exception {
        // Given
        Long orderId = 999L;

        when(paymentService.getPaymentDetails(orderId))
                .thenThrow(new BusinessException(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found for Order ID: " + orderId));

        // When & Then
        mockMvc.perform(get("/api/v1/payment/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.PAYMENT_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PAYMENT_NOT_FOUND.getMessage()));

        verify(paymentService, times(1)).getPaymentDetails(orderId);
    }
}
