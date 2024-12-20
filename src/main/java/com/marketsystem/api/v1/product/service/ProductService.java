package com.marketsystem.api.v1.product.service;

import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.product.dto.ProductRequestDto;
import com.marketsystem.api.v1.product.dto.ProductResponseDto;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.mapper.ProductMapper;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public void createProduct(List<ProductRequestDto.Save> productRequestDtos) {
        List<Product> products= productMapper.toEntityList(productRequestDtos);
        productRepository.saveAll(products);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.toDtoList(products);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAvailablePurchaseProducts() {
        List<Product> products = productRepository.findByStockGreaterThanEqual(0);
        return productMapper.toDtoList(products);
    }

    @Transactional
    public void updateProduct(ProductRequestDto.Update productDto) {
        Product updateProduct = productRepository.findById(productDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + productDto.getId()));
        if(productDto.getStock() != null) {
            if(updateProduct.getStock() + productDto.getStock() < 0)
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            productDto.setStock(updateProduct.getStock() + productDto.getStock());
        }
        productMapper.updateEntityFromDto(productDto, updateProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}