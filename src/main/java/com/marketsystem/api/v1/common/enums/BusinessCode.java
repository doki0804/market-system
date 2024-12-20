package com.marketsystem.api.v1.common.enums;

public enum BusinessCode {
    SUCCESS(200, "SUCCESS"),
    UPDATE(200, "UPDATE"),
    CREATED(201, "CREATED"),
    PRODUCT_DELETED(200, "PRODUCT_DELETED"),
    CART_CLEARED(200, "CART_CLEARED");

    private final int status;
    private final String message;

    BusinessCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
