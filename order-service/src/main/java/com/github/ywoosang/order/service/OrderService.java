package com.github.ywoosang.order.service;

import com.github.ywoosang.order.dto.OrderRequest;
import com.github.ywoosang.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final SnsPublisherService snsPublisherService;

    public OrderService(SnsPublisherService snsPublisherService) {
        this.snsPublisherService = snsPublisherService;
    }

    public Order createOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        
        Order order = new Order(
                orderId,
                request.getUserId(),
                request.getProductId(),
                request.getQuantity(),
                request.getTotalAmount(),
                "CREATED"
        );

        logger.info("Creating order: {}", orderId);

        snsPublisherService.publishOrderEvent(order);

        logger.info("Order created and event published: {}", orderId);

        return order;
    }
}

