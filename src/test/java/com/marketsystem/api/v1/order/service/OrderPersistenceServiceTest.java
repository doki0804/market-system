package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import com.marketsystem.api.v1.order.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderPersistenceServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderUpdateService orderUpdateService;

    @InjectMocks
    private OrderPersistenceService orderPersistenceService;

    @Captor
    private ArgumentCaptor<Orders> ordersCaptor;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .name("tester")
                .build();

        OrderDraft draft = OrderDraft.builder()
                .customer(customer)
                .totalAmount(5000L)
                .orderItems(new ArrayList<>())
                .cartItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(101L)
                .productId(1001L)
                .productName("Product A")
                .productPrice(1000L)
                .quantity(3)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(102L)
                .productId(1002L)
                .productName("Product B")
                .productPrice(2000L)
                .quantity(1)
                .build();

        draft.getOrderItems().add(item1);
        draft.getOrderItems().add(item2);

        Orders savedOrder = Orders.builder()
                .id(201L)
                .customer(customer)
                .totalAmount(draft.getTotalAmount())
                .status(OrderStatus.CREATED)
                .orderItems(new ArrayList<>()) // Assuming Cascade saves
                .build();

        when(ordersRepository.save(any(Orders.class))).thenReturn(savedOrder);

        // When
        Orders result = orderPersistenceService.createOrder(draft);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(customer, result.getCustomer());
        assertEquals(draft.getTotalAmount(), result.getTotalAmount());

        verify(ordersRepository, times(1)).save(ordersCaptor.capture());
        Orders capturedOrder = ordersCaptor.getValue();
        assertEquals(customer, capturedOrder.getCustomer());
        assertEquals(draft.getTotalAmount(), capturedOrder.getTotalAmount());
        assertEquals(OrderStatus.CREATED, capturedOrder.getStatus());
        assertEquals(2, capturedOrder.getOrderItems().size());
        assertTrue(capturedOrder.getOrderItems().contains(item1));
        assertTrue(capturedOrder.getOrderItems().contains(item2));
    }

    @Test
    void finalizeOrder_paymentSuccess() {
        // Given
        Customer customer = Customer.builder()
                .id(2L)
                .name("tester")
                .build();

        Orders order = Orders.builder()
                .id(202L)
                .status(OrderStatus.CREATED)
                .customer(customer)
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(103L)
                .productId(2001L)
                .productName("Product C")
                .productPrice(3000L)
                .quantity(1)
                .build();

        order.getOrderItems().add(item1);

        PaymentResponseDto paymentResponse = new PaymentResponseDto("SUCCESS", "TX54321", "Payment successful");

        // When
        orderPersistenceService.finalizeOrder(order, paymentResponse);

        // Then
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(order, capturedPayment.getOrder());
        assertEquals("TX54321", capturedPayment.getTransactionId());
        assertEquals("Payment successful", capturedPayment.getMessage());
        assertEquals(PaymentStatus.SUCCESS, capturedPayment.getStatus());

        verify(orderUpdateService, times(1)).handlePaymentSuccess(order);
        verify(orderUpdateService, never()).handlePaymentFailure(any());
    }

    @Test
    void finalizeOrder_paymentFailure() {
        // Given
        Customer customer = Customer.builder()
                .id(3L)
                .name("tester")
                .build();

        Orders order = Orders.builder()
                .id(203L)
                .status(OrderStatus.CREATED)
                .customer(customer)
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(104L)
                .productId(2002L)
                .productName("Product D")
                .productPrice(3500L)
                .quantity(2)
                .build();

        order.getOrderItems().add(item1);

        PaymentResponseDto paymentResponse = new PaymentResponseDto("FAILED", null, "Payment failed");

        // When
        orderPersistenceService.finalizeOrder(order, paymentResponse);

        // Then
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        assertEquals(order, capturedPayment.getOrder());
        assertNull(capturedPayment.getTransactionId());
        assertEquals("Payment failed", capturedPayment.getMessage());
        assertEquals(PaymentStatus.FAILED, capturedPayment.getStatus());

        verify(orderUpdateService, times(1)).handlePaymentFailure(order);
        verify(orderUpdateService, never()).handlePaymentSuccess(any());
    }
}
