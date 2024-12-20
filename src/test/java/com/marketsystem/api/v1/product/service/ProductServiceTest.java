package com.marketsystem.api.v1.product.service;

import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.product.dto.ProductRequestDto;
import com.marketsystem.api.v1.product.dto.ProductResponseDto;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.mapper.ProductMapper;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private ProductRequestDto.Save createProductSaveDto(String name, String description, Long price, Integer stock) {
        return new ProductRequestDto.Save(name, description, price, stock);
    }

    private ProductRequestDto.Update createProductUpdateDto(Long id, String name, String description, Long price, Integer stock) {
        return new ProductRequestDto.Update(id, name, description, price, stock);
    }

    private Product createProductEntity(String name, String description, Long price, Integer stock) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();
    }

    private ProductResponseDto createProductResponseDto(Long id, String name, String description, Long price, Integer stock, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ProductResponseDto(id, name, description, price, stock, createdAt, updatedAt);
    }

    // createProduct 메서드 테스트

    @Test
    void createProduct_Success(){
        // Given
        List<ProductRequestDto.Save> productRequestDtos = List.of(
                createProductSaveDto("Product A", "Description A", 1000L, 10),
                createProductSaveDto("Product B", "Description B", 2000L, 20)
        );
        List<Product> products = List.of(
                createProductEntity("Product A", "Description A", 1000L, 10),
                createProductEntity("Product B", "Description B", 2000L, 20)
        );

        when(productMapper.toEntityList(productRequestDtos)).thenReturn(products);
        when(productRepository.saveAll(products)).thenReturn(products);

        // When
        productService.createProduct(productRequestDtos);

        // Then
        verify(productMapper, times(1)).toEntityList(productRequestDtos);
        verify(productRepository, times(1)).saveAll(products);
    }

    @Test
    void createProduct_SaveAllThrowsException(){
        // Given
        List<ProductRequestDto.Save> productRequestDtos = List.of(
                createProductSaveDto("Product A", "Description A", 1000L, 10)
        );
        List<Product> products = List.of(
                createProductEntity("Product A", "Description A", 1000L, 10)
        );

        when(productMapper.toEntityList(productRequestDtos)).thenReturn(products);
        when(productRepository.saveAll(products)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.createProduct(productRequestDtos);
        });

        assertEquals("Database error", exception.getMessage());
        verify(productMapper, times(1)).toEntityList(productRequestDtos);
        verify(productRepository, times(1)).saveAll(products);
    }

    // getProduct 메서드 테스트

    @Test
    void getProduct_Success(){
        // Given
        Long productId = 1L;
        Product product = createProductEntity("Product A", "Description A", 1000L, 10);
        ProductResponseDto productResponseDto = createProductResponseDto(productId, "Product A", "Description A", 1000L, 10, LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productResponseDto);

        // When
        ProductResponseDto result = productService.getProduct(productId);

        // Then
        assertEquals(productResponseDto, result);
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).toDto(product);
    }

    @Test
    void getProduct_NotFound(){
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.getProduct(productId);
        });

        assertEquals("Invalid product ID: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, never()).toDto(any());
    }

    // getAllProducts 메서드 테스트

    @Test
    void getAllProducts_Success(){
        // Given
        List<Product> products = List.of(
                createProductEntity("Product A", "Description A", 1000L, 10),
                createProductEntity("Product B", "Description B", 2000L, 20)
        );
        List<ProductResponseDto> productResponseDtos = List.of(
                createProductResponseDto(1L, "Product A", "Description A", 1000L, 10, LocalDateTime.now(), LocalDateTime.now()),
                createProductResponseDto(2L, "Product B", "Description B", 2000L, 20, LocalDateTime.now(), LocalDateTime.now())
        );

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDtoList(products)).thenReturn(productResponseDtos);

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertEquals(productResponseDtos, result);
        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(1)).toDtoList(products);
    }

    @Test
    void getAllProducts_FindAllThrowsException(){
        // Given
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getAllProducts();
        });

        assertEquals("Database error", exception.getMessage());
        verify(productRepository, times(1)).findAll();
        verify(productMapper, never()).toDtoList(any());
    }

    // updateProduct 메서드 테스트

    @Test
    void updateProduct_Success_WithStockUpdate(){
        // Given
        Long productId = 1L;
        ProductRequestDto.Update productUpdateDto = createProductUpdateDto(productId, "Product A Updated", "Description A Updated", 1500L, 5); // Increase stock by 5
        Product existingProduct = createProductEntity("Product A", "Description A", 1000L, 10);
        Product updatedProduct = createProductEntity("Product A Updated", "Description A Updated", 1500L, 15);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productMapper).updateEntityFromDto(productUpdateDto, existingProduct);

        // When
        productService.updateProduct(productUpdateDto);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntityFromDto(productUpdateDto, existingProduct);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_Success_WithStockDecrease(){
        // Given
        Long productId = 1L;
        ProductRequestDto.Update productUpdateDto = createProductUpdateDto(productId, "Product A Updated", "Description A Updated", 1500L, -5); // Decrease stock by 5
        Product existingProduct = createProductEntity("Product A", "Description A", 1000L, 10);
        Product updatedProduct = createProductEntity("Product A Updated", "Description A Updated", 1500L, 5);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productMapper).updateEntityFromDto(productUpdateDto, existingProduct);

        // When
        productService.updateProduct(productUpdateDto);

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, times(1)).updateEntityFromDto(productUpdateDto, existingProduct);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_OutOfStock(){
        // Given
        Long productId = 1L;
        ProductRequestDto.Update productUpdateDto = createProductUpdateDto(productId, "Product A Updated", "Description A Updated", 1500L, -15); // Decrease stock by 15
        Product existingProduct = createProductEntity("Product A", "Description A", 1000L, 10); // Current stock = 10

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            productService.updateProduct(productUpdateDto);
        });

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, never()).updateEntityFromDto(any(), any());
    }

    @Test
    void updateProduct_NotFound(){
        // Given
        Long productId = 1L;
        ProductRequestDto.Update productUpdateDto = createProductUpdateDto(productId, "Product A Updated", "Description A Updated", 1500L, 5);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.updateProduct(productUpdateDto);
        });

        assertEquals("Invalid product ID: " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productMapper, never()).updateEntityFromDto(any(), any());
    }

    // deleteProduct 메서드 테스트

    @Test
    void deleteProduct_Success(){
        // Given
        Long productId = 1L;
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_NotFound(){
        // Given
        Long productId = 1L;
        doThrow(new EmptyResultDataAccessException(1)).when(productRepository).deleteById(productId);

        // When & Then
        EmptyResultDataAccessException exception = assertThrows(EmptyResultDataAccessException.class, () -> {
            productService.deleteProduct(productId);
        });

        assertEquals(1, exception.getExpectedSize());
        verify(productRepository, times(1)).deleteById(productId);
    }
}
