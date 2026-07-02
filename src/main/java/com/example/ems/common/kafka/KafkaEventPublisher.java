package com.example.ems.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka event publisher responsible for sending serialized event envelopes
 * to Kafka topics. This is the only class that directly interacts with KafkaTemplate.
 */
@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a serialized event to the given topic with the specified partition key.
     *
     * @param topic        the Kafka topic
     * @param partitionKey the partition key for ordering
     * @param payload      the serialized JSON payload
     * @return a CompletableFuture for async result handling
     */
    public CompletableFuture<SendResult<String, String>> publish(String topic,
                                                                  String partitionKey,
                                                                  String payload) {
        log.debug("Publishing event to topic={}, key={}", topic, partitionKey);

        return kafkaTemplate.send(topic, partitionKey, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event to topic={}, key={}: {}",
                                topic, partitionKey, ex.getMessage());
                    } else {
                        log.info("Event published to topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
