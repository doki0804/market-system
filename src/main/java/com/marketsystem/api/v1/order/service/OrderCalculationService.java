package com.marketsystem.api.v1.order.service;

import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.cart.service.CartService;
import com.marketsystem.api.v1.common.exception.BusinessException;
import com.marketsystem.api.v1.common.exception.ErrorCode;
import com.marketsystem.api.v1.customer.entity.Customer;
import com.marketsystem.api.v1.customer.repository.CustomerRepository;
import com.marketsystem.api.v1.order.entity.OrderDraft;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.product.entity.Product;
import com.marketsystem.api.v1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCalculationService {

    private final CartService cartService;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public OrderDraft calculateOrderInfo(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND, "Customer ID: " + customerId));

        List<Cart> cart = cartService.getCartItemsEntity(customerId);
        if (cart.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY, "No items in cart");
        }

        long totalAmount = 0L;
        List<OrderItem> orderItemList = new ArrayList<>();

        for (Cart en : cart) {
            Product product = en.getProduct();

            // 재고 체크
            if (product.getStock() < en.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK, "Product ID: " + product.getId() +
                        ", Requested: " + en.getQuantity() + ", Available: " + product.getStock());
            }

            // 총 금액 계산
            long lineAmount = product.getPrice() * en.getQuantity();
            totalAmount += lineAmount;

            // 주문 항목 생성
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(en.getQuantity())
                    .build();

            orderItemList.add(orderItem);
        }

        return new OrderDraft(customer, totalAmount, orderItemList, cart);
    }
}
