package com.marketsystem.api.v1.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private Long price;
        @NotNull
        private Integer stock;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Update{
        @NotNull
        private Long id;
        private String name;
        private String description;
        private Long price;
        private Integer stock;
    }
}