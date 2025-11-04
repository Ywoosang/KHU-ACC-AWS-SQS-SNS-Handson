package com.github.ywoosang.stock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("totalAmount")
    private Double totalAmount;
    
    @JsonProperty("orderStatus")
    private String orderStatus;

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}

