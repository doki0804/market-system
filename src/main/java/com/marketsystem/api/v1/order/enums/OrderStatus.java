package com.marketsystem.api.v1.order.enums;

public enum OrderStatus {
    CREATED("주문 생성"),
    ERROR("시스템 에러"),
    PAID("결제 완료"),
    FAILED("결제 실패");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
