package com.marketsystem.api.v1.common.enums;

public enum ErrorCode {

    OUT_OF_STOCK(200, "Out of stock"),

    BAD_PARAMETER(400, "Bad parameter"),

    CUSTOMER_NOT_FOUND(404, "Customer not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    ORDER_NOT_FOUND(404, "Order not found"),
    PAYMENT_NOT_FOUND(404, "Payment not found"),

    CART_EMPTY(400, "Cart is empty"),

    NOT_FOUND(404, "Resource not found"),
    CONFLICT(409, "Conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
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