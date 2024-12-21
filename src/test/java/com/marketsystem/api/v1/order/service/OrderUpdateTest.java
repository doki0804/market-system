package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderUpdateServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderUpdateService orderUpdateService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handlePaymentFailure() {
        // Given
        Orders order = Orders.builder()
                .id(1L)
                .status(com.marketsystem.api.v1.order.enums.OrderStatus.CREATED)
                .orderItems(new java.util.ArrayList<>())
                .customer(com.marketsystem.api.v1.customer.entity.Customer.builder().id(1L).build())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(101L)
                .productId(1001L)
                .productName("Product A")
                .productPrice(1000L)
                .quantity(2)
                .build();

        order.getOrderItems().add(item1);

        // When
        orderUpdateService.handlePaymentFailure(order);

        // Then
        assertEquals(com.marketsystem.api.v1.order.enums.OrderStatus.FAILED, order.getStatus());
        assertTrue(order.getOrderItems().isEmpty());

        verify(ordersRepository, times(1)).save(order);
    }

    @Test
    void handlePaymentSuccess() {
        // Given
        Customer customer = Customer.builder()
                .id(2L)
                .name("tester")
                .build();

        Orders order = Orders.builder()
                .id(2L)
                .status(OrderStatus.CREATED)
                .customer(customer)
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(102L)
                .productId(2001L)
                .productName("Product B")
                .productPrice(2000L)
                .quantity(3)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(103L)
                .productId(2002L)
                .productName("Product C")
                .productPrice(1500L)
                .quantity(1)
                .build();

        order.getOrderItems().add(item1);
        order.getOrderItems().add(item2);

        // 실제 Product 인스턴스 생성
        Product product1 = Product.builder()
                .id(2001L)
                .name("Product B")
                .price(2000L)
                .stock(10)
                .build();;

        Product product2 = Product.builder()
                .id(2002L)
                .name("Product C")
                .price(1500L)
                .stock(5)
                .build();

        when(productRepository.findById(2001L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2002L)).thenReturn(Optional.of(product2));

        // When
        orderUpdateService.handlePaymentSuccess(order);

        // Then
        assertEquals(OrderStatus.PAID, order.getStatus());

        assertEquals(7, product1.getStock()); // 10 - 3
        assertEquals(4, product2.getStock()); // 5 -1

        verify(productRepository, never()).save(any(Product.class));

        verify(ordersRepository, times(1)).save(order);

        verify(cartService, times(1)).clearCart(customer.getId());
    }

    @Test
    void handlePaymentSuccess_productNotFound() {
        // Given
        Customer customer = Customer.builder()
                .id(3L)
                .name("tester")
                .build();

        Orders order = Orders.builder()
                .id(3L)
                .status(OrderStatus.CREATED)
                .customer(customer)
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(104L)
                .productId(3001L)
                .productName("Product D")
                .productPrice(2500L)
                .quantity(2)
                .build();

        order.getOrderItems().add(item1);

        when(productRepository.findById(3001L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderUpdateService.handlePaymentSuccess(order);
        });

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        assertEquals("Product ID: 3001", exception.getMessage());

        verify(productRepository, never()).save(any(Product.class));

        verify(ordersRepository, never()).save(any(Orders.class));

        verify(cartService, never()).clearCart(anyLong());
    }
}
