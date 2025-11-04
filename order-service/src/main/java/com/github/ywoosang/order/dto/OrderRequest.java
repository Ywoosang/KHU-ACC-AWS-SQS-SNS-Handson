package com.github.ywoosang.order.dto;

public class OrderRequest {
    private String userId;
    private String productId;
    private Integer quantity;
    private Double totalAmount;

    public OrderRequest() {
    }

    public OrderRequest(String userId, String productId, Integer quantity, Double totalAmount) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}

