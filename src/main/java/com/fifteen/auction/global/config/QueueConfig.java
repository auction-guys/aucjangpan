package com.fifteen.auction.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class QueueConfig {

    public static final String TYPE_ID_HEADER_KEY = "MessageTypeId";
    public static final String TYPE_ID_BID_REQUEST_EVENT = "BidRequestEvent";
    public static final String TYPE_ID_BUY_NOW_REQUEST_EVENT = "BuyNowRequestEvent";

    @Value("${cloud.aws.scheduler.access-key-id}")
    private String accessKeyId;

    @Value("${cloud.aws.scheduler.secret-access-key}")
    private String secretAccessKey;

    @Bean
//    @Profile("prod")
    public SqsClient sqsProdClient() {
        return SqsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .build();
    }

//    @Bean
//    @Profile("local")
    public SqsClient sqsLocalClient() {
        return SqsClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .endpointOverride(URI.create("http://localhost:4566"))
                .build();
    }
}
