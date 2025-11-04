package com.github.ywoosang.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ywoosang.payment.dto.SnsNotification;
import com.github.ywoosang.payment.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class PaymentConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumerService.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.payment.queue.url}")
    private String paymentQueueUrl;

    public PaymentConsumerService(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void consumePaymentQueue() {
        try {
            logger.debug("[Payment] Polling queue: {}", paymentQueueUrl);
            
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(paymentQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(20)
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                logger.debug("[Payment] No messages in queue");
                return;
            }

            logger.info("[Payment] ✅ Received {} message(s) from SQS queue", messages.size());

            for (Message message : messages) {
                try {
                    logger.info("[Payment] 📨 Message received - MessageId: {}, ReceiptHandle: {}", 
                            message.messageId(), message.receiptHandle().substring(0, Math.min(20, message.receiptHandle().length())) + "...");
                    logger.debug("[Payment] Raw message body: {}", message.body());
                    
                    // SNS Notification 형식으로 파싱
                    SnsNotification snsNotification = objectMapper.readValue(message.body(), SnsNotification.class);
                    
                    logger.debug("[Payment] SNS Notification - Type: {}, Subject: {}, MessageId: {}", 
                            snsNotification.getType(), snsNotification.getSubject(), snsNotification.getMessageId());
                    
                    // SNS Notification의 Message 필드에서 실제 Order 데이터 추출
                    String orderJson = snsNotification.getMessage();
                    Order order = objectMapper.readValue(orderJson, Order.class);
                    
                    logger.info("[Payment] 📦 Parsed Order - OrderId: {}, UserId: {}, ProductId: {}, Quantity: {}, Amount: {}", 
                            order.getOrderId(), order.getUserId(), order.getProductId(), 
                            order.getQuantity(), order.getTotalAmount());
                    
                    processPayment(order);
                    
                    deleteMessage(paymentQueueUrl, message.receiptHandle());
                    
                    logger.info("[Payment] ✅ Successfully processed and deleted message for order: {}", 
                            order.getOrderId());
                            
                } catch (Exception e) {
                    logger.error("[Payment] ❌ Failed to process message. MessageId: {}, Body: {}", 
                            message.messageId(), message.body(), e);
                }
            }
        } catch (SqsException e) {
            logger.error("[Payment] ❌ Error receiving messages from queue: {}", paymentQueueUrl, e);
        }
    }

    private void processPayment(Order order) {
        logger.info("[Payment] 💳 Processing payment for order: {}, Amount: {}", 
                order.getOrderId(), order.getTotalAmount());
        try {
            Thread.sleep(100);
            logger.info("[Payment] ✅ Payment completed for order: {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[Payment] Payment processing interrupted", e);
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
            logger.error("[Payment] Failed to delete message from queue", e);
        }
    }
}

