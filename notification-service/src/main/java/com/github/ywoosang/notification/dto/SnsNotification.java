package com.github.ywoosang.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnsNotification {
    
    @JsonProperty("Type")
    private String type;
    
    @JsonProperty("MessageId")
    private String messageId;
    
    @JsonProperty("TopicArn")
    private String topicArn;
    
    @JsonProperty("Subject")
    private String subject;
    
    @JsonProperty("Message")
    private String message;
    
    @JsonProperty("Timestamp")
    private String timestamp;
    
    @JsonProperty("SignatureVersion")
    private String signatureVersion;
    
    @JsonProperty("Signature")
    private String signature;
    
    @JsonProperty("SigningCertURL")
    private String signingCertUrl;
    
    @JsonProperty("UnsubscribeURL")
    private String unsubscribeUrl;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}

