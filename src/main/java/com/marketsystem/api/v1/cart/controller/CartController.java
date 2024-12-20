package com.marketsystem.api.v1.cart.controller;

import com.marketsystem.api.v1.cart.dto.CartItemDto;
import com.marketsystem.api.v1.cart.dto.CartRequestDto;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.utils.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping(value = "{customerId}")
    public ResponseEntity<?> getCartList(@PathVariable Long customerId){
        CartItemDto res = cartService.getCartItems(customerId);
        return ResponseEntity.ok().body(CommonResponse.success(BusinessCode.SUCCESS,res));
    }

    @PostMapping
    public ResponseEntity<?> addCart(@Valid @RequestBody CartRequestDto cartRequestDto){
        cartService.addToCart(cartRequestDto);
        return ResponseEntity.ok().body(CommonResponse.success(BusinessCode.SUCCESS));
    }

    @DeleteMapping("{customerId}")
    public ResponseEntity<?> clearCart(@PathVariable Long customerId){
        cartService.clearCart(customerId);
        return ResponseEntity.ok().body(CommonResponse.success(BusinessCode.CART_CLEARED));
    }
}
