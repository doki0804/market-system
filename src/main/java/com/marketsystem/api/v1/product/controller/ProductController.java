package com.marketsystem.api.v1.product.controller;

import com.marketsystem.api.v1.common.utils.CommonResponse;
import com.marketsystem.api.v1.product.dto.ProductRequestDto;
import com.marketsystem.api.v1.product.dto.ProductResponseDto;
import com.marketsystem.api.v1.product.service.ProductService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/vi/product")
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProducts(@RequestBody List<ProductRequestDto.Save> saveDtos) {
        productService.createProduct(saveDtos);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable long productId) {
        ProductResponseDto res = productService.getProduct(productId);
        return ResponseEntity.ok(CommonResponse.success(res));
    }

    @GetMapping
    public ResponseEntity<?> getProductList() {
        List<ProductResponseDto> res =productService.getAllProducts();
        return ResponseEntity.ok(CommonResponse.success(res));
    }

    @PatchMapping
    public ResponseEntity<?> updateProduct(@RequestBody ProductRequestDto.Update updateDto) {
        productService.updateProduct(updateDto);
        return ResponseEntity.ok(CommonResponse.success());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProduct(@RequestParam long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok(CommonResponse.success());
    }
}
