package com.marketsystem.api.v1.cart.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class CartItemDto {
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private int productQuantity;
    private Long productPrice;
    private Integer productStock;
}
