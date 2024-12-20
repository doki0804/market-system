package com.marketsystem.api.v1.order.mapper;

import com.marketsystem.api.v1.order.dto.*;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Orders;
import com.marketsystem.api.v1.order.entity.Payment;
import com.marketsystem.api.v1.order.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "status", target = "orderStatus")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderResponseDto toOrderResponseDto(Orders order);

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "productPrice", target = "productPrice")
    @Mapping(source = "quantity", target = "quantity")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

    List<OrderItemResponseDto> toOrderItemResponseDtoList(List<OrderItem> orderItems);

    @Mapping(source = "id", target = "paymentId")
    @Mapping(source = "transactionId", target = "transactionId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "createdAt", target = "paymentDate")
    @Mapping(source = "order", target = "order")
    PaymentDetailsDto toPaymentDetailsDto(Payment payment);

    default OrderCreateResponseDto toOrderCreateResponseDto(Orders order, PaymentResponseDto paymentResponseDto) {
        Long customerId = order.getCustomer().getId();
        String customerName = order.getCustomer().getName();
        String statusStr = (order.getStatus() == OrderStatus.PAID) ? "SUCCESS" : "FAILED";
        String message = paymentResponseDto.getMessage();
        String transactionId = paymentResponseDto.getTransactionId();
        Long totalPrice = (order.getStatus() == OrderStatus.PAID) ? order.getTotalAmount() : null;
        List<OrderItemResponseDto> orderItemResponseDtoList = toOrderItemResponseDtoList(order.getOrderItems());
        return new OrderCreateResponseDto(customerId, customerName, order.getId(), transactionId, statusStr, message, totalPrice, orderItemResponseDtoList);
    }
}