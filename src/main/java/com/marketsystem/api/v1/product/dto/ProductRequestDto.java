package com.marketsystem.api.v1.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ProductRequestDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Save{
        private String name;
        private String description;
        private Long price;
        private Integer stock;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Update{
        private Long id;
        private String name;
        private String description;
        private Long price;
        private Integer stock;
    }
}