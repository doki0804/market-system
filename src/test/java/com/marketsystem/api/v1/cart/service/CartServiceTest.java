package com.marketsystem.api.v1.cart.service;

import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.dto.CartRequestDto;
import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.entity.CartId;
import com.marketsystem.api.v1.cart.repository.CartRepository;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import com.marketsystem.api.v1.product.dto.ProductDto;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    // 헬퍼 메서드: 테스트용 CartRequestDto 생성
    private CartRequestDto createCartRequestDto(Long customerId, List<CartRequestDto.Product> products) {
        CartRequestDto dto = new CartRequestDto();
        dto.setCustomerId(customerId);
        dto.setProductList(products);
        return dto;
    }

    // 헬퍼 메서드: 테스트용 CartRequestDto.Product 생성
    private CartRequestDto.Product createCartProduct(Long productId, int quantity) {
        CartRequestDto.Product product = new CartRequestDto.Product();
        product.setProductId(productId);
        product.setQuantity(quantity);
        return product;
    }

    // 헬퍼 메서드: 테스트용 Customer 엔터티 생성
    private Customer createCustomer(Long id, String name) {
        Customer customer = Customer.builder()
                .id(id)
                .name(name)
                .build();
        return customer;
    }

    // 헬퍼 메서드: 테스트용 Product 엔터티 생성
    private Product createProductEntity(Long id, String name, String description, Long price, Integer stock) {
        Product product = Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
        return product;
    }

    // 헬퍼 메서드: 테스트용 Cart 엔터티 생성
    private Cart createCartEntity(Long customerId, Long productId, int quantity) {
        CartId cartId = new CartId(customerId, productId);
        Customer customer = createCustomer(customerId, "Customer " + customerId);
        Product product = createProductEntity(productId, "Product " + productId, "Description " + productId, 1000L * productId, 20);
        Cart cart = Cart.builder()
                .id(cartId)
                .customer(customer)
                .product(product)
                .quantity(quantity)
                .build();
        return cart;
    }

    // 헬퍼 메서드: 테스트용 ProductDto 생성
    private ProductDto createProductDto(Long id, String name, Long price, Integer stock) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStock(stock);
        return dto;
    }

    // 헬퍼 메서드: 테스트용 CartItemDto 생성
    private CartItemDto createCartItemDto(Long customerId, List<ProductDto> productList) {
        CartItemDto dto = new CartItemDto();
        dto.setCustomerId(customerId);
        dto.setProductList(productList);
        return dto;
    }

    // addToCart 메서드 테스트: 성공
    @Test
    void addToCart_Success() {
        // Given
        Long customerId = 1L;
        List<CartRequestDto.Product> products = List.of(
                createCartProduct(101L, 2),
                createCartProduct(102L, 3)
        );
        CartRequestDto cartRequestDto = createCartRequestDto(customerId, products);

        Customer customer = createCustomer(customerId, "Customer 1");
        Product product101 = createProductEntity(101L, "Product 101", "Description 101", 1000L, 10);
        Product product102 = createProductEntity(102L, "Product 102", "Description 102", 2000L, 20);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllByIdIn(Set.of(101L, 102L))).thenReturn(List.of(product101, product102));

        // 기존 장바구니 항목: product101에 대한 기존 수량 1
        Cart existingCart101 = createCartEntity(customerId, 101L, 1);
        when(cartRepository.findAllById(List.of(new CartId(customerId, 101L), new CartId(customerId, 102L))))
                .thenReturn(List.of(existingCart101));

        // When
        cartService.addToCart(cartRequestDto);

        // Then
        // 고객 조회 확인
        verify(customerRepository, times(1)).findById(customerId);
        // 제품 조회 확인
        verify(productRepository, times(1)).findAllByIdIn(Set.of(101L, 102L));
        // 기존 장바구니 조회 확인
        verify(cartRepository, times(1)).findAllById(anyList());

        // 저장할 장바구니 항목 캡처
        ArgumentCaptor<List<Cart>> cartCaptor = ArgumentCaptor.forClass(List.class);
        verify(cartRepository, times(1)).saveAll(cartCaptor.capture());

        List<Cart> savedCarts = cartCaptor.getValue();
        assertEquals(2, savedCarts.size());

        // product101의 수량이 기존 1 + 2 = 3으로 업데이트되었는지 확인
        Cart savedCart101 = savedCarts.stream()
                .filter(c -> c.getId().getProductId().equals(101L))
                .findFirst()
                .orElse(null);
        assertNotNull(savedCart101);
        assertEquals(3, savedCart101.getQuantity());

        // product102에 대한 새로운 장바구니 항목이 저장되었는지 확인
        Cart savedCart102 = savedCarts.stream()
                .filter(c -> c.getId().getProductId().equals(102L))
                .findFirst()
                .orElse(null);
        assertNotNull(savedCart102);
        assertEquals(3, savedCart102.getQuantity());
    }

    // addToCart 메서드 테스트: 실패 (Customer Not Found)
    @Test
    void addToCart_CustomerNotFound() {
        // Given
        Long customerId = 1L;
        List<CartRequestDto.Product> products = List.of(
                createCartProduct(101L, 2)
        );
        CartRequestDto cartRequestDto = createCartRequestDto(customerId, products);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.addToCart(cartRequestDto);
        });

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, exception.getErrorCode());

        verify(customerRepository, times(1)).findById(customerId);
        verify(productRepository, never()).findAllByIdIn(anySet());
        verify(cartRepository, never()).findAllById(anyList());
        verify(cartRepository, never()).saveAll(anyList());
    }

    // addToCart 메서드 테스트: 실패 (Product Not Found)
    @Test
    void addToCart_ProductNotFound() {
        // Given
        Long customerId = 1L;
        List<CartRequestDto.Product> products = List.of(
                createCartProduct(101L, 2),
                createCartProduct(999L, 3) // 존재하지 않는 제품 ID
        );
        CartRequestDto cartRequestDto = createCartRequestDto(customerId, products);

        Customer customer = createCustomer(customerId, "Customer 1");
        Product product101 = createProductEntity(101L, "Product 101", "Description 101", 1000L, 10);
        // product999는 존재하지 않음

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllByIdIn(Set.of(101L, 999L))).thenReturn(List.of(product101)); // product999는 반환되지 않음

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.addToCart(cartRequestDto);
        });

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        assertEquals("Product IDs not found: [999]", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(productRepository, times(1)).findAllByIdIn(Set.of(101L, 999L));
        verify(cartRepository, never()).findAllById(anyList());
        verify(cartRepository, never()).saveAll(anyList());
    }

    // addToCart 메서드 테스트: 실패 (Out of Stock)
    @Test
    void addToCart_OutOfStock() {
        // Given
        Long customerId = 1L;
        List<CartRequestDto.Product> products = List.of(
                createCartProduct(101L, 5) // 기존 수량 3 + 추가 5 = 8, 재고 7
        );
        CartRequestDto cartRequestDto = createCartRequestDto(customerId, products);

        Customer customer = createCustomer(customerId, "Customer 1");
        Product product101 = createProductEntity(101L, "Product 101", "Description 101", 1000L, 7);

        // 기존 장바구니 항목: product101에 대한 기존 수량 3
        Cart existingCart101 = createCartEntity(customerId, 101L, 3);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(productRepository.findAllByIdIn(Set.of(101L))).thenReturn(List.of(product101));
        when(cartRepository.findAllById(anyList())).thenReturn(List.of(existingCart101));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.addToCart(cartRequestDto);
        });

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        assertEquals("Requested quantity: 5, Available: 7", exception.getMessage());

        verify(customerRepository, times(1)).findById(customerId);
        verify(productRepository, times(1)).findAllByIdIn(Set.of(101L));
        verify(cartRepository, times(1)).findAllById(anyList());
        verify(cartRepository, never()).saveAll(anyList());
    }

    // getCartItems 메서드 테스트: 성공
    @Test
    void getCartItems_Success() {
        // Given
        Long customerId = 1L;
        Customer customer = createCustomer(customerId, "Customer 1");

        Cart cart1 = createCartEntity(customerId, 101L, 2);
        Cart cart2 = createCartEntity(customerId, 102L, 3);
        List<Cart> cartItems = List.of(cart1, cart2);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerIdWithProduct(customerId)).thenReturn(cartItems);

        // When
        CartItemDto cartItemDto = cartService.getCartItems(customerId);

        // Then
        assertNotNull(cartItemDto);
        assertEquals(customerId, cartItemDto.getCustomerId());

        List<ProductDto> productDtoList = cartItemDto.getProductList();
        assertEquals(2, productDtoList.size());

        // ProductDto가 equals 메서드를 제대로 구현하고 있다고 가정
        ProductDto productDto1 = createProductDto(101L, "Product 101", 101000L, 20);
        ProductDto productDto2 = createProductDto(102L, "Product 102", 102000L, 20);

        assertTrue(productDtoList.containsAll(List.of(productDto1, productDto2)));
    }

    // getCartItems 메서드 테스트: 실패 (Customer Not Found)
    @Test
    void getCartItems_CustomerNotFound() {
        // Given
        Long customerId = 1L;

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            cartService.getCartItems(customerId);
        });

        assertEquals(ErrorCode.CUSTOMER_NOT_FOUND, exception.getErrorCode());

        verify(customerRepository, times(1)).findById(customerId);
        verify(cartRepository, never()).findByCustomerIdWithProduct(anyLong());
    }

    // clearCart 메서드 테스트: 성공
    @Test
    void clearCart_Success() {
        // Given
        Long customerId = 1L;
        List<Cart> cartItems = List.of(
                createCartEntity(customerId, 101L, 2),
                createCartEntity(customerId, 102L, 3)
        );

        when(cartRepository.findByCustomerId(customerId)).thenReturn(cartItems);
        doNothing().when(cartRepository).deleteAll(cartItems);

        // When
        cartService.clearCart(customerId);

        // Then
        verify(cartRepository, times(1)).findByCustomerId(customerId);
        verify(cartRepository, times(1)).deleteAll(cartItems);
    }

    // clearCart 메서드 테스트: 실패 (Exception 발생)
    @Test
    void clearCart_ExceptionHandling() {
        // Given
        Long customerId = 1L;

        when(cartRepository.findByCustomerId(customerId)).thenThrow(new RuntimeException("Database error"));

        // When
        // clearCart는 예외를 다시 던지지 않고 로그로만 기록하므로, 테스트에서는 예외가 발생하지 않는지 확인
        assertDoesNotThrow(() -> {
            cartService.clearCart(customerId);
        });

        // Then
        verify(cartRepository, times(1)).findByCustomerId(customerId);
        verify(cartRepository, never()).deleteAll(anyList());
    }
}

