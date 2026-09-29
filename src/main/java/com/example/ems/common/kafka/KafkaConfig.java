package com.example.ems.common.kafka;

import com.example.ems.common.event.EventTypes;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Kafka configuration for topic creation and infrastructure setup.
 * Producer and consumer factories are auto-configured by Spring Boot.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${kafka.topics.partitions:3}")
    private int defaultPartitions;

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name(EventTypes.NOTIFICATION_CREATED)
                .partitions(defaultPartitions)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketReplyCreatedTopic() {
        return TopicBuilder.name(EventTypes.TICKET_REPLY_CREATED)
                .partitions(defaultPartitions)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketCreatedTopic() {
        return TopicBuilder.name(EventTypes.TICKET_CREATED)
                .partitions(defaultPartitions)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationCreatedDlqTopic() {
        return TopicBuilder.name(EventTypes.NOTIFICATION_CREATED + EventTypes.DLQ_SUFFIX)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketReplyCreatedDlqTopic() {
        return TopicBuilder.name(EventTypes.TICKET_REPLY_CREATED + EventTypes.DLQ_SUFFIX)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
