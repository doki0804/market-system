package com.marketsystem.api.v1.order.controller;

import com.marketsystem.api.v1.common.enums.BusinessCode;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.common.utils.CommonResponse;
import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderRequestDto;
import com.marketsystem.api.v1.order.dto.PaymentDetailsDto;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import com.marketsystem.api.v1.order.service.OrderService;
import com.marketsystem.api.v1.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 주문 생성 API
     * Request Body: { "customerId": Long }
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        OrderCreateResponseDto res = orderService.placeOrder(requestDto.getCustomerId());
        if(res.getStatus().equals("SUCCESS")) {
            return ResponseEntity.ok(CommonResponse.success(BusinessCode.SUCCESS, res));
        } else {
            return ResponseEntity.internalServerError().body(CommonResponse.error(ErrorCode.PAYMENT_SERVER_ERROR.getStatus(), ErrorCode.PAYMENT_SERVER_ERROR.getMessage(),res));
        }
    }

    @GetMapping("{orderId}")
    public ResponseEntity<?> getPaymentDetail(@PathVariable Long orderId) {
        PaymentDetailsDto res = paymentService.getPaymentDetails(orderId);
        return ResponseEntity.ok().body(CommonResponse.success(BusinessCode.SUCCESS,res));
    }
}
