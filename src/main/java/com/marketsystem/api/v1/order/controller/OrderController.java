package com.marketsystem.api.v1.order.controller;

import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.utils.CommonResponse;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 상세 조회 API
     * Path Variable: orderId
     * Response: 주문 상세 정보
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS,response));
    }
}
