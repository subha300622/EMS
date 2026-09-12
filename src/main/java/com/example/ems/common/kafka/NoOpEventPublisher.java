package com.example.ems.common.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * No-op event publisher active when app.kafka.enabled is false (or missing).
 */
@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public CompletableFuture<Void> publish(String topic, String partitionKey, String payload) {
        log.debug("Event publishing is disabled. Event to topic={} with key={} was ignored.", topic, partitionKey);
        return CompletableFuture.completedFuture(null);
    }
}
