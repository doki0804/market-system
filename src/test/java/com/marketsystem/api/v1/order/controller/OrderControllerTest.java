package com.marketsystem.api.v1.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.GlobalExceptionHandler;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Success")
    void getOrderDetail_Success() throws Exception {
        // Given
        Long orderId = 1L;
        OrderResponseDto orderResponseDto = OrderResponseDto.builder()
                .orderId(orderId)
                .orderStatus("PAID")
                .totalAmount(10000L)
                .orderItems(null) // 필요한 경우 설정
                .build();

        when(orderService.getOrderDetail(orderId)).thenReturn(orderResponseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.SUCCESS.getStatus()))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.data.totalAmount").value(10000L));

        verify(orderService, times(1)).getOrderDetail(orderId);
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} - Order Not Found")
    void getOrderDetail_OrderNotFound() throws Exception {
        // Given
        Long orderId = 999L;

        when(orderService.getOrderDetail(orderId))
                .thenThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order ID: " + orderId));

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ORDER_NOT_FOUND.getMessage()));

        verify(orderService, times(1)).getOrderDetail(orderId);
    }
}
