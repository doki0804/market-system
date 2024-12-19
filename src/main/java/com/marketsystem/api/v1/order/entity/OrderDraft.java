package com.marketsystem.api.v1.order.entity;

import com.marketsystem.api.v1.cart.entity.Cart;
import com.marketsystem.api.v1.customer.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderDraft {
    private Customer customer;
    private long totalAmount;
    private List<OrderItem> orderItems;
    private List<Cart> cartItems;
}