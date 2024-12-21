package com.marketsystem.api.v1.product.controller;

import com.marketsystem.api.v1.common.dto.PaginatedResponse;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.utils.CommonResponse;
import com.marketsystem.api.v1.product.dto.ProductRequestDto;
import com.marketsystem.api.v1.product.dto.ProductResponseDto;
import com.marketsystem.api.v1.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProducts(@Valid @RequestBody List<ProductRequestDto.Save> saveDtos) {
        productService.createProduct(saveDtos);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable long productId) {
        ProductResponseDto res = productService.getProduct(productId);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS,res));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProductList() {
        var res =productService.getAllProducts();
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS, res));
    }

    @GetMapping
    public ResponseEntity<?> getAvailablePurchaseProductList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PaginatedResponse<ProductResponseDto> res = productService.getAvailablePurchaseProducts(pageable);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS, res));
    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@Valid @RequestBody ProductRequestDto.Update updateDto) {
        productService.updateProduct(updateDto);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.UPDATE));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProduct(@RequestParam long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.PRODUCT_DELETED));
    }
}
