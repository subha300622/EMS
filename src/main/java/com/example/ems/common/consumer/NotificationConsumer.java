package com.example.ems.common.consumer;

import com.example.ems.common.event.EventEnvelope;
import com.example.ems.common.event.EventTypes;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for notification events.
 * Processes notification creation events from the ems.notification.created
 * topic.
 */
@Component
public class NotificationConsumer extends AbstractKafkaConsumer {

    private static final String CONSUMER_GROUP = "ems-notification-consumer";

    public NotificationConsumer(ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper) {
        super(processedEventRepository, objectMapper);
    }

    @Override
    protected String getConsumerGroup() {
        return CONSUMER_GROUP;
    }

    @KafkaListener(topics = EventTypes.NOTIFICATION_CREATED, groupId = CONSUMER_GROUP)
    public void onMessage(String message) {
        consume(message);
    }

    @Override
    protected void handleEvent(EventEnvelope<Object> envelope) {
        log.info("Notification event received: eventId={}, correlationId={}, payload={}",
                envelope.getEventId(),
                envelope.getCorrelationId(),
                envelope.getPayload());

    }
}
