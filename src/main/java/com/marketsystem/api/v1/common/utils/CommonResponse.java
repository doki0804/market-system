package com.marketsystem.api.v1.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.marketsystem.api.v1.common.enums.BusinessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 포맷 클래스
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(BusinessCode businessCode, T data) {
        return new CommonResponse<>(businessCode.getStatus(), businessCode.getMessage(), data);
    }

    public static CommonResponse<Void> success(BusinessCode businessCode) {
        return new CommonResponse<>( businessCode.getStatus(), businessCode.getMessage(), null);
    }

    public static <T> CommonResponse<T> error(int code, String message, T data) {
        return new CommonResponse<>(code, message, data);
    }
}