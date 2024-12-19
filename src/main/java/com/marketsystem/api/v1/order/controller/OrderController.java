package com.marketsystem.api.v1.order.controller;

import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderRequestDto;
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
     * 주문 생성 API
     * Request Body: { "customerId": Long }
     * Response: { "orderId": Long, "status": "SUCCESS" or "FAILED" }
     */
    @PostMapping
    public ResponseEntity<OrderCreateResponseDto> placeOrder(@RequestBody OrderRequestDto requestDto) {
        OrderCreateResponseDto response = orderService.placeOrder(requestDto.getCustomerId());
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상세 조회 API
     * Path Variable: orderId
     * Response: 주문 상세 정보
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderDetail(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderDetail(orderId);
        return ResponseEntity.ok(response);
    }
}
