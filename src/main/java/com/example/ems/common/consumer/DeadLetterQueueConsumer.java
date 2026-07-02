package com.example.ems.common.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Dead Letter Queue topics.
 * Logs failed events for monitoring and operational visibility.
 */
@Component
public class DeadLetterQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueConsumer.class);

    @KafkaListener(
            topics = {
                    "ems.notification.created.dlq",
                    "ems.support.ticket.reply.created.dlq"
            },
            groupId = "ems-dlq-consumer"
    )
    public void onDeadLetterMessage(String message) {
        log.error("DLQ event received — requires manual intervention: {}", message);
        // TODO: Persist to a DLQ audit table, send alerts, increment metrics
    }
}
