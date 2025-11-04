package com.github.ywoosang.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ywoosang.notification.dto.SnsNotification;
import com.github.ywoosang.notification.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class NotificationConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumerService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.notification.queue.url}")
    private String notificationQueueUrl;

    public NotificationConsumerService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void consumeNotificationQueue() {
        try {
            logger.debug("[Notification] Polling queue: {}", notificationQueueUrl);
            
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(notificationQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                logger.debug("[Notification] No messages in queue");
                return;
            }

            logger.info("[Notification] ✅ Received {} message(s) from SQS queue", messages.size());

            for (Message message : messages) {
                try {
                    logger.info("[Notification] 📨 Message received - MessageId: {}, ReceiptHandle: {}", 
                            message.messageId(), message.receiptHandle().substring(0, Math.min(20, message.receiptHandle().length())) + "...");
                    logger.debug("[Notification] Raw message body: {}", message.body());
                    
                    // SNS Notification 형식으로 파싱
                    SnsNotification snsNotification = objectMapper.readValue(message.body(), SnsNotification.class);
                    
                    logger.debug("[Notification] SNS Notification - Type: {}, Subject: {}, MessageId: {}", 
                            snsNotification.getType(), snsNotification.getSubject(), snsNotification.getMessageId());
                    
                    // SNS Notification의 Message 필드에서 실제 Order 데이터 추출
                    String orderJson = snsNotification.getMessage();
                    Order order = objectMapper.readValue(orderJson, Order.class);
                    
                    logger.info("[Notification] 📦 Parsed Order - OrderId: {}, UserId: {}, ProductId: {}, Quantity: {}, Amount: {}", 
                            order.getOrderId(), order.getUserId(), order.getProductId(), 
                            order.getQuantity(), order.getTotalAmount());
                    
                    processNotification(order);
                    
                    deleteMessage(notificationQueueUrl, message.receiptHandle());
                    
                    logger.info("[Notification] ✅ Successfully processed and deleted message for order: {}", 
                            order.getOrderId());
                            
                } catch (Exception e) {
                    logger.error("[Notification] ❌ Failed to process message. MessageId: {}, Body: {}", 
                            message.messageId(), message.body(), e);
                }
            }
        } catch (SqsException e) {
            logger.error("[Notification] ❌ Error receiving messages from queue: {}", notificationQueueUrl, e);
        }
    }

    private void processNotification(Order order) {
        logger.info("[Notification] 📧 Sending notification for order: {}, UserId: {}", 
                order.getOrderId(), order.getUserId());
        try {
            Thread.sleep(100);
            logger.info("[Notification] ✅ Notification sent for order: {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Notification] Notification processing interrupted", e);
        }
    }

    private void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();

            sqsClient.deleteMessage(deleteRequest);
        } catch (SqsException e) {
            logger.error("[Notification] Failed to delete message from queue", e);
        }
    }
}

