package com.github.ywoosang.order.controller;

import com.github.ywoosang.order.dto.OrderRequest;
import com.github.ywoosang.order.dto.OrderResponse;
import com.github.ywoosang.order.model.Order;
import com.github.ywoosang.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        
        OrderResponse response = new OrderResponse(
                order.getOrderId(),
                "Order created successfully and event published to SNS"
        );
        
        return ResponseEntity.ok(response);
    }
}

