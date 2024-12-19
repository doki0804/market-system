package com.marketsystem.api.v1.order.mapper;

import com.marketsystem.api.v1.order.dto.OrderCreateResponseDto;
import com.marketsystem.api.v1.order.dto.OrderItemResponseDto;
import com.marketsystem.api.v1.order.dto.OrderResponseDto;
import com.marketsystem.api.v1.order.entity.OrderItem;
import com.marketsystem.api.v1.order.entity.Orders;
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

    default OrderCreateResponseDto toOrderCreateResponseDto(Orders order) {
        String statusStr = (order.getStatus() == OrderStatus.PAID) ? "SUCCESS" : "FAILED";
        return new OrderCreateResponseDto(order.getId(), statusStr);
    }
}