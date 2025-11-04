package com.github.ywoosang.stock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ywoosang.stock.dto.SnsNotification;
import com.github.ywoosang.stock.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class StockConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(StockConsumerService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.stock.queue.url}")
    private String stockQueueUrl;

    public StockConsumerService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void consumeStockQueue() {
        try {
            logger.debug("[Stock] Polling queue: {}", stockQueueUrl);
            
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(stockQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                logger.debug("[Stock] No messages in queue");
                return;
            }

            logger.info("[Stock] ✅ Received {} message(s) from SQS queue", messages.size());

            for (Message message : messages) {
                try {
                    logger.info("[Stock] 📨 Message received - MessageId: {}, ReceiptHandle: {}", 
                            message.messageId(), message.receiptHandle().substring(0, Math.min(20, message.receiptHandle().length())) + "...");
                    logger.debug("[Stock] Raw message body: {}", message.body());
                    
                    // SNS Notification 형식으로 파싱
                    SnsNotification snsNotification = objectMapper.readValue(message.body(), SnsNotification.class);
                    
                    logger.debug("[Stock] SNS Notification - Type: {}, Subject: {}, MessageId: {}", 
                            snsNotification.getType(), snsNotification.getSubject(), snsNotification.getMessageId());
                    
                    // SNS Notification의 Message 필드에서 실제 Order 데이터 추출
                    String orderJson = snsNotification.getMessage();
                    Order order = objectMapper.readValue(orderJson, Order.class);
                    
                    logger.info("[Stock] 📦 Parsed Order - OrderId: {}, UserId: {}, ProductId: {}, Quantity: {}, Amount: {}", 
                            order.getOrderId(), order.getUserId(), order.getProductId(), 
                            order.getQuantity(), order.getTotalAmount());
                    
                    processStock(order);
                    
                    deleteMessage(stockQueueUrl, message.receiptHandle());
                    
                    logger.info("[Stock] ✅ Successfully processed and deleted message for order: {}", 
                            order.getOrderId());
                            
                } catch (Exception e) {
                    logger.error("[Stock] ❌ Failed to process message. MessageId: {}, Body: {}", 
                            message.messageId(), message.body(), e);
                }
            }
        } catch (SqsException e) {
            logger.error("[Stock] ❌ Error receiving messages from queue: {}", stockQueueUrl, e);
        }
    }

    private void processStock(Order order) {
        logger.info("[Stock] 📦 Processing stock update for order: {}, ProductId: {}, Quantity: {}", 
                order.getOrderId(), order.getProductId(), order.getQuantity());
        try {
            Thread.sleep(100);
            logger.info("[Stock] ✅ Stock updated for order: {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Stock] Stock processing interrupted", e);
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
            logger.error("[Stock] Failed to delete message from queue", e);
        }
    }
}

