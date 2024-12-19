package com.marketsystem.api.v1.order.enums;

public enum PaymentStatus {

    SUCCESS("결제 성공"),
    FAILED("결제 실패");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
