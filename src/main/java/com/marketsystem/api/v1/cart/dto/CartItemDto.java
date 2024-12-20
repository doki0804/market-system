package com.marketsystem.api.v1.cart.dto;

import com.marketsystem.api.v1.product.dto.ProductDto;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long customerId;
    private List<ProductDto> productList;
}
