package com.github.ywoosang.order.dto;

public class OrderResponse {
    private String orderId;
    private String message;

    public OrderResponse() {
    }

    public OrderResponse(String orderId, String message) {
        this.orderId = orderId;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

