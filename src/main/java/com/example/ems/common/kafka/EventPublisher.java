package com.example.ems.common.kafka;

import java.util.concurrent.CompletableFuture;

public interface EventPublisher {
    CompletableFuture<?> publish(String topic, String partitionKey, String payload);
}
