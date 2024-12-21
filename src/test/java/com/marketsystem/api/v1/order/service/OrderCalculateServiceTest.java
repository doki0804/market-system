package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.entity.CartId;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCalculationServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderCalculationService orderCalculationService;

    @Captor
    private ArgumentCaptor<Long> customerIdCaptor;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void calculateOrderInfo_ShouldReturnOrderDraft_WhenAllConditionsMet() {
        // Given
        Long customerId = 1L;
        Customer customer = Customer.builder()
                .id(customerId)
                .name("John Doe")
                .build();

        Product product1 = Product.builder()
                .id(101L)
                .name("Product A")
                .price(1000L)
                .stock(10)
                .build();

        Product product2 = Product.builder()
                .id(102L)
                .name("Product B")
                .price(2000L)
                .stock(5)
                .build();

        CartId cartId1 = new CartId(1L, 101L);
        CartId cartId2 = new CartId(1L, 102L);

        Cart cartItem1 = Cart.builder()
                .id(cartId1)
                .product(product1)
                .quantity(2)
                .build();

        Cart cartItem2 = Cart.builder()
                .id(cartId2)
                .product(product2)
                .quantity(3)
                .build();

        List<Cart> cartItems = Arrays.asList(cartItem1, cartItem2);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartService.getCartItemsEntity(customerId)).thenReturn(cartItems);

        // When
        OrderDraft draft = orderCalculationService.calculateOrderInfo(customerId);

        // Then
        assertNotNull(draft);
        assertEquals(customer, draft.getCustomer());
        assertEquals(8000L, draft.getTotalAmount()); // 2*1000 + 3*2000
        assertEquals(2, draft.getOrderItems().size());
        assertEquals(2, draft.getCartItems().size());

        OrderItem oi1 = draft.getOrderItems().get(0);
        assertEquals(product1.getId(), oi1.getProductId());
        assertEquals(product1.getName(), oi1.getProductName());
        assertEquals(product1.getPrice(), oi1.getProductPrice());
        assertEquals(cartItem1.getQuantity(), oi1.getQuantity());

        OrderItem oi2 = draft.getOrderItems().get(1);
        assertEquals(product2.getId(), oi2.getProductId());
        assertEquals(product2.getName(), oi2.getProductName());
        assertEquals(product2.getPrice(), oi2.getProductPrice());
        assertEquals(cartItem2.getQuantity(), oi2.getQuantity());

        verify(customerRepository, times(1)).findById(customerId);
        verify(cartService, times(1)).getCartItemsEntity(customerId);
    }

    @Test
    void calculateOrderInfo_ShouldThrowBusinessException_WhenCustomerNotFound() {
        // Given
        Long customerId = 2L;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderCalculationService.calculateOrderInfo(customerId);
        });

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, exception.getErrorCode());
        assertEquals("Customer ID: " + customerId, exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(cartService, never()).getCartItemsEntity(anyLong());
    }

    @Test
    void calculateOrderInfo_ShouldThrowBusinessException_WhenCartIsEmpty() {
        // Given
        Long customerId = 3L;
        Customer customer = Customer.builder()
                .id(customerId)
                .name("Jane Doe")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartService.getCartItemsEntity(customerId)).thenReturn(Arrays.asList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderCalculationService.calculateOrderInfo(customerId);
        });

        assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
        assertEquals("No items in cart", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(cartService, times(1)).getCartItemsEntity(customerId);
    }

    @Test
    void calculateOrderInfo_ShouldThrowBusinessException_WhenProductOutOfStock() {
        // Given
        Long customerId = 4L;
        Customer customer = Customer.builder()
                .id(customerId)
                .name("Alice Smith")
                .build();

        Product product1 = Product.builder()
                .id(103L)
                .name("Product C")
                .price(1500L)
                .stock(1)
                .build();

        CartId cartId = new CartId(4L, 103L);

        Cart cartItem1 = Cart.builder()
                .id(cartId)
                .product(product1)
                .quantity(2) // Exceeds stock
                .build();

        List<Cart> cartItems = Arrays.asList(cartItem1);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartService.getCartItemsEntity(customerId)).thenReturn(cartItems);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            orderCalculationService.calculateOrderInfo(customerId);
        });

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        assertEquals("Product ID: " + product1.getId() +
                        ", Requested: " + cartItem1.getQuantity() + ", Available: " + product1.getStock(),
                exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(cartService, times(1)).getCartItemsEntity(customerId);
    }
}
