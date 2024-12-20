package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import com.marketsystem.api.v1.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final OrderUpdateService orderUpdateService;

    /**
     * 계산된 주문 정보를 데이터베이스에 저장하는 메서드.
     * @param draft 계산된 주문 정보 객체
     * @return 저장된 주문 엔터티
     */
    @Transactional
    public Orders createOrder(OrderDraft draft) {
        // 새로운 주문 엔터티 생성 및 초기 상태 설정
        Orders order = Orders.builder()
                .customer(draft.getCustomer())
                .totalAmount(draft.getTotalAmount())
                .status(OrderStatus.CREATED)
                .build();

        // 주문 초안에 포함된 각 주문 항목을 주문 엔터티에 추가
        for (OrderItem oi : draft.getOrderItems()) {
            // OrderItem은 Orders 엔터티에 CascadeType.ALL 설정되어 있어 별도로 저장할 필요 없음
            order.addOrderItem(oi);
        }

        ordersRepository.save(order);
        return order;
    }

    /**
     * 결제 응답을 바탕으로 주문의 상태를 결정 메서드.
     * 결제 상태에 따라 주문의 상태를 업데이트하고, 결제 정보를 저장
     * @param order            상태를 확정할 주문 정보 엔터티
     * @param paymentResponse  결제 처리 결과를 담은 PaymentResponseDto 객체
     */
    @Transactional
    public void finalizeOrder(Orders order, PaymentResponseDto paymentResponse) {
        // 결제 정보 엔터티 생성
        Payment payment = Payment.builder()
                .order(order)
                .transactionId(paymentResponse.getTransactionId())
                .message(paymentResponse.getMessage())
                .status("SUCCESS".equalsIgnoreCase(paymentResponse.getStatus()) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();

        // 결제 정보 저장
        paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // 결제 성공시 order update 메서드
            orderUpdateService.handlePaymentSuccess(order);
        } else {
            // 결제 실패시 order update 메서드
            orderUpdateService.handlePaymentFailure(order);
        }
    }
}
