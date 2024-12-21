package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import com.marketsystem.api.v1.order.mapper.OrderMapper;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderCalculationService orderCalculationService;

    @Mock
    private OrderPersistenceService orderPersistenceService;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void placeOrder_success() {
        // Given
        Long customerId = 1L;

        Customer customer = Customer.builder()
                .id(customerId)
                .name("tester")
                .build();

        OrderDraft draft = OrderDraft.builder()
                .customer(customer)
                .totalAmount(3000L)
                .orderItems(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .build();

        Orders savedOrder = Orders.builder()
                .id(100L)
                .customer(customer)
                .totalAmount(draft.getTotalAmount())
                .status(OrderStatus.CREATED)
                .orderItems(new ArrayList<>())
                .build();

        PaymentResponseDto paymentResponse = new PaymentResponseDto("SUCCESS", "TX99999", "Payment successful");

        OrderCreateResponseDto responseDto = OrderCreateResponseDto.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .orderId(savedOrder.getId())
                .transactionId(paymentResponse.getTransactionId())
                .status("PAID")
                .message(paymentResponse.getMessage())
                .totalPrice(savedOrder.getTotalAmount())
                .orderItems(new ArrayList<>())
                .build();

        when(orderCalculationService.calculateOrderInfo(customerId)).thenReturn(draft);
        when(orderPersistenceService.createOrder(draft)).thenReturn(savedOrder);
        when(paymentService.requestPayment(savedOrder)).thenReturn(paymentResponse);
        doNothing().when(orderPersistenceService).finalizeOrder(savedOrder, paymentResponse);
        when(orderMapper.toOrderCreateResponseDto(savedOrder, paymentResponse)).thenReturn(responseDto);

        // When
        OrderCreateResponseDto result = orderService.placeOrder(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customer.getId(), result.getCustomerId());
        assertEquals(customer.getName(), result.getCustomerName());
        assertEquals(savedOrder.getId(), result.getOrderId());
        assertEquals(paymentResponse.getTransactionId(), result.getTransactionId());
        assertEquals("PAID", result.getStatus());
        assertEquals(paymentResponse.getMessage(), result.getMessage());
        assertEquals(savedOrder.getTotalAmount(), result.getTotalPrice());
        assertTrue(result.getOrderItems().isEmpty()); // As per test setup

        verify(orderCalculationService, times(1)).calculateOrderInfo(customerId);
        verify(orderPersistenceService, times(1)).createOrder(draft);
        verify(paymentService, times(1)).requestPayment(savedOrder);
        verify(orderPersistenceService, times(1)).finalizeOrder(savedOrder, paymentResponse);
        verify(orderMapper, times(1)).toOrderCreateResponseDto(savedOrder, paymentResponse);
    }

    @Test
    void placeOrder_calculationFailed() {
        // Given
        Long customerId = 2L;

        when(orderCalculationService.calculateOrderInfo(customerId)).thenThrow(new BusinessException(ErrorCode.CART_EMPTY, "No items in cart"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.placeOrder(customerId));

        assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
        assertEquals("No items in cart", exception.getMessage());

        verify(orderCalculationService, times(1)).calculateOrderInfo(customerId);
        verify(orderPersistenceService, never()).createOrder(any());
        verify(paymentService, never()).requestPayment(any());
        verify(orderPersistenceService, never()).finalizeOrder(any(), any());
        verify(orderMapper, never()).toOrderCreateResponseDto(any(), any());
    }

    @Test
    void placeOrder_paymentResponseFailed() {
        // Given
        Long customerId = 3L;

        Customer customer = Customer.builder()
                .id(customerId)
                .name("tester")
                .build();

        OrderDraft draft = OrderDraft.builder()
                .customer(customer)
                .totalAmount(4000L)
                .orderItems(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .build();

        Orders savedOrder = Orders.builder()
                .id(101L)
                .customer(customer)
                .totalAmount(draft.getTotalAmount())
                .status(OrderStatus.CREATED)
                .orderItems(new ArrayList<>())
                .build();

        PaymentResponseDto paymentResponse = new PaymentResponseDto("FAILED", null, "Payment failed");

        OrderCreateResponseDto responseDto = OrderCreateResponseDto.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .orderId(savedOrder.getId())
                .transactionId(paymentResponse.getTransactionId())
                .status("FAILED")
                .message(paymentResponse.getMessage())
                .totalPrice(savedOrder.getTotalAmount())
                .orderItems(new ArrayList<>())
                .build();

        when(orderCalculationService.calculateOrderInfo(customerId)).thenReturn(draft);
        when(orderPersistenceService.createOrder(draft)).thenReturn(savedOrder);
        when(paymentService.requestPayment(savedOrder)).thenReturn(paymentResponse);
        doNothing().when(orderPersistenceService).finalizeOrder(savedOrder, paymentResponse);
        when(orderMapper.toOrderCreateResponseDto(savedOrder, paymentResponse)).thenReturn(responseDto);

        // When
        OrderCreateResponseDto result = orderService.placeOrder(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customer.getId(), result.getCustomerId());
        assertEquals(customer.getName(), result.getCustomerName());
        assertEquals(savedOrder.getId(), result.getOrderId());
        assertEquals(paymentResponse.getTransactionId(), result.getTransactionId());
        assertEquals("FAILED", result.getStatus());
        assertEquals(paymentResponse.getMessage(), result.getMessage());
        assertEquals(savedOrder.getTotalAmount(), result.getTotalPrice());
        assertTrue(result.getOrderItems().isEmpty());

        verify(orderCalculationService, times(1)).calculateOrderInfo(customerId);
        verify(orderPersistenceService, times(1)).createOrder(draft);
        verify(paymentService, times(1)).requestPayment(savedOrder);
        verify(orderPersistenceService, times(1)).finalizeOrder(savedOrder, paymentResponse);
        verify(orderMapper, times(1)).toOrderCreateResponseDto(savedOrder, paymentResponse);
    }

    @Test
    void getOrderDetail_orderExists() {
        // Given
        Long orderId = 100L;
        Customer customer = Customer.builder()
                .id(1L)
                .name("tester")
                .build();

        Orders order = Orders.builder()
                .id(orderId)
                .customer(customer)
                .totalAmount(10000L)
                .status(OrderStatus.PAID)
                .orderItems(new ArrayList<>())
                .build();

        OrderResponseDto responseDto = OrderResponseDto.builder()
                .orderId(orderId)
                .orderStatus(order.getStatus().toString())
                .totalAmount(order.getTotalAmount())
                .orderItems(new ArrayList<>())
                .build();

        when(ordersRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponseDto(order)).thenReturn(responseDto);

        // When
        OrderResponseDto result = orderService.getOrderDetail(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(order.getStatus().toString(), result.getOrderStatus());
        assertEquals(order.getTotalAmount(), result.getTotalAmount());
        assertTrue(result.getOrderItems().isEmpty());

        verify(ordersRepository, times(1)).findById(orderId);
        verify(orderMapper, times(1)).toOrderResponseDto(order);
    }

    @Test
    void getOrderDetail_orderNotFound() {
        // Given
        Long orderId = 102L;

        when(ordersRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> orderService.getOrderDetail(orderId));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        assertEquals("Order ID: " + orderId, exception.getMessage());

        verify(ordersRepository, times(1)).findById(orderId);
        verify(orderMapper, never()).toOrderResponseDto(any());
    }
}
