package com.marketsystem.api.v1.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class CustomerRequestDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Save{
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Update{
        private Long id;
        private String name;
    }
}
