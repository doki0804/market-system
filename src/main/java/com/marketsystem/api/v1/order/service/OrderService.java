package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCalculationService orderCalculationService;
    private final OrderPersistenceService orderPersistenceService;
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;

    public OrderCreateResponseDto placeOrder(Long customerId) {
        // 주문 정보 계산
        OrderDraft draft = orderCalculationService.calculateOrderInfo(customerId);

        // 주문 생성 및 저장
        Orders order = orderPersistenceService.createOrder(draft);

        // 결제 요청
        PaymentResponseDto paymentResponse = paymentService.requestPayment(order);

        // 결제 결과 반영 및 재고 차감
        orderPersistenceService.finalizeOrder(order, paymentResponse);

        // 주문 생성 응답 DTO 매핑 및 반환
        return orderMapper.toOrderCreateResponseDto(order);
    }

    public OrderCreateResponseDto placeOrderTransactional(Long customerId) {
        return placeOrder(customerId);
    }

    public OrderResponseDto getOrderDetail(Long orderId) {
        Orders order = orderPersistenceService.findOrderById(orderId);
        return orderMapper.toOrderResponseDto(order);
    }
}
