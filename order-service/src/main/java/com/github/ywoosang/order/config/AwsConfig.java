package com.github.ywoosang.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.credentials.access-key-id}")
    private String accessKeyId;

    @Value("${aws.credentials.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public SnsClient snsClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}

