package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.ErrorCode;
import com.marketsystem.api.v1.order.dto.PaymentResponseDto;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import com.marketsystem.api.v1.order.enums.PaymentStatus;
import com.marketsystem.api.v1.order.repository.OrderItemRepository;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import com.marketsystem.api.v1.order.repository.PaymentRepository;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;

    @Transactional
    public Orders createOrder(OrderDraft draft) {
        Orders order = Orders.builder()
                .customer(draft.getCustomer())
                .totalAmount(draft.getTotalAmount())
                .status(OrderStatus.CREATED)
                .build();

        for (OrderItem oi : draft.getOrderItems()) {
            order.addOrderItem(oi);
        }

        ordersRepository.save(order);
        return order;
    }

    @Transactional
    public void finalizeOrder(Orders order, PaymentResponseDto paymentResponse) {
        Payment payment = Payment.builder()
                .order(order)
                .transactionId(paymentResponse.getTransactionId())
                .message(paymentResponse.getMessage())
                .status("SUCCESS".equalsIgnoreCase(paymentResponse.getStatus()) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            order.markAsPaid();
            // 재고 차감
            for (OrderItem oi : order.getOrderItems()) {
                Product product = productRepository.findById(oi.getProductId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product ID: " + oi.getProductId()));
                product.decreaseStock(oi.getQuantity());
            }
        } else {
            order.markAsFailed();
        }

        // 결제 정보 저장
        paymentRepository.save(payment);

        // 장바구니 비우기
        cartService.clearCart(order.getCustomer().getId());
    }

    @Transactional(readOnly = true)
    public Orders findOrderById(Long orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "Order ID: " + orderId));
    }
}
