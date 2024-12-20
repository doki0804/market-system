package com.marketsystem.api.v1.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.dto.CartRequestDto;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.product.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartRequestDto createCartRequestDto(Long customerId, List<CartRequestDto.Product> products) {
        CartRequestDto dto = new CartRequestDto();
        dto.setCustomerId(customerId);
        dto.setProductList(products);
        return dto;
    }

    private CartRequestDto.Product createCartProduct(Long productId, int quantity) {
        CartRequestDto.Product product = new CartRequestDto.Product();
        product.setProductId(productId);
        product.setQuantity(quantity);
        return product;
    }

    private CartItemDto createCartItemDto(Long customerId, List<ProductDto> productList) {
        CartItemDto dto = new CartItemDto();
        dto.setCustomerId(customerId);
        dto.setProductList(productList);
        return dto;
    }

    private ProductDto createProductDto(Long id, String name, Long price, Integer stock) {
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStock(stock);
        return dto;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // GET /api/v1/cart/{customerId} 테스트: 성공
    @Test
    void getCartList_Success() throws Exception {
        // Given
        Long customerId = 1L;
        List<ProductDto> products = List.of(
                createProductDto(101L, "Product A", 1000L, 10),
                createProductDto(102L, "Product B", 2000L, 20)
        );
        CartItemDto cartItemDto = createCartItemDto(customerId, products);

        when(cartService.getCartItems(customerId)).thenReturn(cartItemDto);

        // When & Then
        mockMvc.perform(get("/api/v1/cart/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.SUCCESS.getStatus()))
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.productList").isArray())
                .andExpect(jsonPath("$.data.productList.length()").value(products.size()))
                .andExpect(jsonPath("$.data.productList[0].id").value(products.get(0).getId()))
                .andExpect(jsonPath("$.data.productList[0].name").value(products.get(0).getName()))
                .andExpect(jsonPath("$.data.productList[0].price").value(products.get(0).getPrice()))
                .andExpect(jsonPath("$.data.productList[0].stock").value(products.get(0).getStock()))
                .andExpect(jsonPath("$.data.productList[1].id").value(products.get(1).getId()))
                .andExpect(jsonPath("$.data.productList[1].name").value(products.get(1).getName()))
                .andExpect(jsonPath("$.data.productList[1].price").value(products.get(1).getPrice()))
                .andExpect(jsonPath("$.data.productList[1].stock").value(products.get(1).getStock()));

        verify(cartService, times(1)).getCartItems(customerId);
    }

    // GET /api/v1/cart/{customerId} 테스트: 실패 (CUSTOMER_NOT_FOUND)
    @Test
    void getCartList_CustomerNotFound() throws Exception {
        // Given
        Long customerId = 1L;

        when(cartService.getCartItems(customerId))
                .thenThrow(new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/cart/{customerId}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.CUSTOMER_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CUSTOMER_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.details").value(ErrorCode.CUSTOMER_NOT_FOUND.getMessage()));

        verify(cartService, times(1)).getCartItems(customerId);
    }

    // POST /api/v1/cart 테스트: 성공
    @Test
    void addCart_Success() throws Exception {
        // Given
        CartRequestDto.Product product1 = createCartProduct(101L, 2);
        CartRequestDto.Product product2 = createCartProduct(102L, 3);
        CartRequestDto cartRequestDto = createCartRequestDto(1L, List.of(product1, product2));

        doNothing().when(cartService).addToCart(any(CartRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.SUCCESS.getStatus()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(cartService, times(1)).addToCart(any(CartRequestDto.class));
    }

    // POST /api/v1/cart 테스트: 실패 (Product Not Found)
    @Test
    void addCart_ProductNotFound() throws Exception {
        // Given
        CartRequestDto.Product product1 = createCartProduct(101L, 2);
        CartRequestDto.Product product2 = createCartProduct(999L, 3); // 존재하지 않는 제품 ID
        CartRequestDto cartRequestDto = createCartRequestDto(1L, List.of(product1, product2));

        doThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product IDs not found: [999]"))
                .when(cartService).addToCart(any(CartRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.details").value("Product IDs not found: [999]"));

        verify(cartService, times(1)).addToCart(any(CartRequestDto.class));
    }

    // POST /api/v1/cart 테스트: 실패 (Out of Stock)
    @Test
    void addCart_OutOfStock() throws Exception {
        // Given
        CartRequestDto.Product product1 = createCartProduct(101L, 2);
        CartRequestDto.Product product2 = createCartProduct(102L, 50); // 재고 부족
        CartRequestDto cartRequestDto = createCartRequestDto(1L, List.of(product1, product2));

        doThrow(new BusinessException(ErrorCode.OUT_OF_STOCK, "Requested quantity: 50, Available: 20"))
                .when(cartService).addToCart(any(CartRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.OUT_OF_STOCK.getStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.OUT_OF_STOCK.getMessage()))
                .andExpect(jsonPath("$.details").value("Requested quantity: 50, Available: 20"));

        verify(cartService, times(1)).addToCart(any(CartRequestDto.class));
    }

    // DELETE /api/v1/cart/{customerId} 테스트: 성공
    @Test
    void clearCart_Success() throws Exception {
        // Given
        Long customerId = 1L;

        doNothing().when(cartService).clearCart(customerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/cart/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BusinessCode.CART_CLEARED.getStatus()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(cartService, times(1)).clearCart(customerId);
    }

    // DELETE /api/v1/cart/{customerId} 테스트: 실패 (CUSTOMER_NOT_FOUND)
    @Test
    void clearCart_CustomerNotFound() throws Exception {
        // Given
        Long customerId = 1L;

        doThrow(new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer not found"))
                .when(cartService).clearCart(customerId);

        // When & Then
        mockMvc.perform(delete("/api/v1/cart/{customerId}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.CUSTOMER_NOT_FOUND.getStatus()))
                .andExpect(jsonPath("$.message").value("Customer not found"));

        verify(cartService, times(1)).clearCart(customerId);
    }
}
