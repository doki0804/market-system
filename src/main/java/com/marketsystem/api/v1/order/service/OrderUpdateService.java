package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.enums.ErrorCode;
import com.marketsystem.api.v1.order.repository.OrdersRepository;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderUpdateService {

    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private static final Logger logger = LoggerFactory.getLogger(OrderUpdateService.class);

    /**
     * 결제 실패 시 Order 상태를 FAILED로 변경하고, OrderItem을 삭제하는 메서드.
     */
    @Transactional
    public void handlePaymentFailure(Orders order) {
        order.markAsFailed();
        // 주문 항목을 컬렉션에서 제거
        order.getOrderItems().clear();
        ordersRepository.save(order);
        logger.info("Order ID: {} order FAILED", order.getId());
    }

    /**
     * 결제 성공 시 Order 상태를 PAID로 변경하고, 재고를 차감하며, 장바구니를 비우는 메서드.
     */
    @Transactional
    public void handlePaymentSuccess(Orders order) {
        order.markAsPaid();
        // 재고 차감
        for (OrderItem oi : order.getOrderItems()) {
            Product product = productRepository.findById(oi.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product ID: " + oi.getProductId()));
            product.decreaseStock(oi.getQuantity());
        }
        ordersRepository.save(order);

        // 별도의 트랜잭션에서 장바구니 비우기
        cartService.clearCart(order.getCustomer().getId());
    }
}
