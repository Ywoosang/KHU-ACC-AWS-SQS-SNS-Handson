package com.github.ywoosang.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ywoosang.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
public class SnsPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(SnsPublisherService.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topic.arn}")
    private String topicArn;

    public SnsPublisherService(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    public void publishOrderEvent(Order order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .subject("Order Created")
                    .build();

            PublishResponse response = snsClient.publish(request);
            
            logger.info("Order event published to SNS. MessageId: {}, OrderId: {}", 
                    response.messageId(), order.getOrderId());
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize order to JSON", e);
            throw new RuntimeException("Failed to serialize order", e);
        } catch (SnsException e) {
            logger.error("Failed to publish message to SNS", e);
            throw new RuntimeException("Failed to publish message to SNS", e);
        }
    }
}

