package com.marketsystem.api.v1.cart.controller;

import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.dto.CartRequestDto;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.utils.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> getCartList(@PathVariable Long customerId){
        List<CartItemDto> res = cartService.getCartItems(customerId);
        return ResponseEntity.ok().body(CommonResponse.success(res));
    }

    @PostMapping
    public ResponseEntity<?> addCart(@RequestBody CartRequestDto cartRequestDto){
        System.out.println(cartRequestDto);
        cartService.addToCart(cartRequestDto.getCustomerId(), cartRequestDto.getProductId(), cartRequestDto.getQuantity());
        return ResponseEntity.ok().body(CommonResponse.success());
    }
}
