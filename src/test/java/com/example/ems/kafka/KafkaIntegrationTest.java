package com.example.ems.kafka;

import com.example.ems.common.consumer.ProcessedEventRepository;
import com.example.ems.common.event.*;
import com.example.ems.common.kafka.EventPublisher;
import com.example.ems.common.outbox.OutboxEvent;
import com.example.ems.common.outbox.OutboxEventRepository;
import com.example.ems.common.outbox.OutboxPublisher;
import com.example.ems.common.outbox.OutboxStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying the full Kafka event-driven pipeline:
 * 1. DomainEventPublisher → Outbox table
 * 2. OutboxPublisher → Kafka broker
 * 3. NotificationConsumer ← Kafka broker → ProcessedEvents table
 *
 * Uses Spring Embedded Kafka so no external broker is needed.
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
                EventTypes.NOTIFICATION_CREATED,
                EventTypes.TICKET_REPLY_CREATED,
                EventTypes.NOTIFICATION_CREATED + ".dlq",
                EventTypes.TICKET_REPLY_CREATED + ".dlq",
                EventTypes.TICKET_CREATED
}, brokerProperties = {
                "listener.security.protocol.map=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT"
})
@DirtiesContext
@ActiveProfiles("kafkatest")
class KafkaIntegrationTest {

        @Autowired
        private DomainEventPublisher domainEventPublisher;

        @Autowired
        private OutboxEventRepository outboxEventRepository;

        @Autowired
        private OutboxPublisher outboxPublisher;

        @Autowired
        private ProcessedEventRepository processedEventRepository;

        @Autowired
        private EventPublisher eventPublisher;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void cleanUp() {
                processedEventRepository.deleteAll();
                outboxEventRepository.deleteAll();
        }

        // -------------------------------------------------------
        // TEST 1: Domain event → Outbox table
        // -------------------------------------------------------
        @Test
        @DisplayName("1. DomainEventPublisher persists event to outbox table with PENDING status")
        void domainEventPublisher_shouldPersistToOutbox() {
                // Given
                NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                1L, "Test Title", "Test Message", "SYSTEM", "HIGH");

                EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                EventTypes.NOTIFICATION_CREATED,
                                "Notification",
                                "1",
                                payload);

                // When
                domainEventPublisher.publish(envelope);

                // Then
                var events = outboxEventRepository.findAll();
                assertEquals(1, events.size(), "Exactly one outbox event should exist");

                OutboxEvent outboxEvent = events.getFirst();
                assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
                assertEquals(EventTypes.NOTIFICATION_CREATED, outboxEvent.getEventType());
                assertEquals("Notification", outboxEvent.getAggregateType());
                assertEquals("1", outboxEvent.getAggregateId());
                assertNotNull(outboxEvent.getPayload());
                assertNotNull(outboxEvent.getCorrelationId());
                assertEquals(0, outboxEvent.getRetryCount());

                System.out.println("✅ TEST 1 PASSED: Domain event persisted to outbox with PENDING status");
        }

        // -------------------------------------------------------
        // TEST 2: Outbox → Kafka (publish pending events)
        // -------------------------------------------------------
        @Test
        @DisplayName("2. OutboxPublisher fetches PENDING events and publishes them to Kafka")
        void outboxPublisher_shouldPublishToKafka() {
                // Given - insert an event to outbox
                NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                2L, "Kafka Test", "Publishing to Kafka", "SYSTEM", "MEDIUM");

                EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                EventTypes.NOTIFICATION_CREATED,
                                "Notification",
                                "2",
                                payload);
                domainEventPublisher.publish(envelope);

                // Verify it's PENDING
                assertEquals(1, outboxEventRepository.countByStatus(OutboxStatus.PENDING));

                // When - run the outbox publisher
                int published = outboxPublisher.publishPendingEvents();

                // Then
                assertEquals(1, published, "One event should have been published");
                assertEquals(0, outboxEventRepository.countByStatus(OutboxStatus.PENDING),
                                "No PENDING events should remain");
                assertEquals(1, outboxEventRepository.countByStatus(OutboxStatus.PUBLISHED),
                                "One event should be PUBLISHED");

                OutboxEvent publishedEvent = outboxEventRepository.findAll().getFirst();
                assertNotNull(publishedEvent.getPublishedAt(), "publishedAt should be set");

                System.out.println("✅ TEST 2 PASSED: Outbox event published to Kafka and marked PUBLISHED");
        }

        // -------------------------------------------------------
        // TEST 3: Full pipeline - Domain → Outbox → Kafka → Consumer
        // -------------------------------------------------------
        @Test
        @DisplayName("3. Full pipeline: event flows from business service through Kafka to consumer")
        void fullPipeline_shouldDeliverEventToConsumer() throws Exception {
                // Given
                NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                3L, "Full Pipeline", "End-to-end test", "SYSTEM", "LOW");

                EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                EventTypes.NOTIFICATION_CREATED,
                                "Notification",
                                "3",
                                payload);

                // When - publish domain event
                domainEventPublisher.publish(envelope);

                // Trigger the outbox publisher
                outboxPublisher.publishPendingEvents();

                // Wait for consumer to process (max 10 seconds)
                UUID eventId = envelope.getEventId();
                boolean processed = false;
                for (int i = 0; i < 20; i++) {
                        if (processedEventRepository.existsByEventIdAndConsumerGroup(
                                        eventId, "ems-notification-consumer")) {
                                processed = true;
                                break;
                        }
                        TimeUnit.MILLISECONDS.sleep(500);
                }

                // Then
                assertTrue(processed, "Event should have been processed by NotificationConsumer");

                // Verify outbox is PUBLISHED
                assertEquals(1, outboxEventRepository.countByStatus(OutboxStatus.PUBLISHED));

                System.out.println(
                                "✅ TEST 3 PASSED: Full pipeline verified - Domain → Outbox → Kafka → Consumer → ProcessedEvents");
        }

        // -------------------------------------------------------
        // TEST 4: Idempotency - duplicate events are skipped
        // -------------------------------------------------------
        @Test
        @DisplayName("4. Idempotent consumer skips duplicate events")
        void consumer_shouldSkipDuplicateEvents() throws Exception {
                // Given
                NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                4L, "Idempotency Test", "Should be processed once", "SYSTEM", "HIGH");

                EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                EventTypes.NOTIFICATION_CREATED,
                                "Notification",
                                "4",
                                payload);

                // First publication
                domainEventPublisher.publish(envelope);
                outboxPublisher.publishPendingEvents();

                // Wait for first processing
                UUID eventId = envelope.getEventId();
                for (int i = 0; i < 20; i++) {
                        if (processedEventRepository.existsByEventIdAndConsumerGroup(
                                        eventId, "ems-notification-consumer")) {
                                break;
                        }
                        TimeUnit.MILLISECONDS.sleep(500);
                }

                long countAfterFirst = processedEventRepository.count();

                // When - publish the same event ID directly to Kafka (simulating duplicate)
                String serialized = objectMapper.writeValueAsString(envelope);
                eventPublisher.publish(
                                EventTypes.NOTIFICATION_CREATED,
                                "4",
                                serialized).get(5, TimeUnit.SECONDS);

                // Wait a bit for potential duplicate processing
                TimeUnit.SECONDS.sleep(2);

                // Then - count should not increase
                long countAfterSecond = processedEventRepository.count();
                assertEquals(countAfterFirst, countAfterSecond,
                                "Duplicate event should be skipped by idempotency check");

                System.out.println("✅ TEST 4 PASSED: Idempotent consumer correctly skipped duplicate event");
        }

        // -------------------------------------------------------
        // TEST 5: Multiple events batch processing
        // -------------------------------------------------------
        @Test
        @DisplayName("5. OutboxPublisher processes multiple events in a single batch")
        void outboxPublisher_shouldHandleBatch() {
                // Given - insert 5 events
                for (int i = 1; i <= 5; i++) {
                        NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                        (long) i, "Batch Event " + i, "Message " + i, "SYSTEM", "MEDIUM");

                        EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                        EventTypes.NOTIFICATION_CREATED,
                                        "Notification",
                                        String.valueOf(i),
                                        payload);
                        domainEventPublisher.publish(envelope);
                }

                assertEquals(5, outboxEventRepository.countByStatus(OutboxStatus.PENDING));

                // When
                int published = outboxPublisher.publishPendingEvents();

                // Then
                assertEquals(5, published, "All 5 events should be published");
                assertEquals(5, outboxEventRepository.countByStatus(OutboxStatus.PUBLISHED));
                assertEquals(0, outboxEventRepository.countByStatus(OutboxStatus.PENDING));

                System.out.println("✅ TEST 5 PASSED: Batch of 5 events processed successfully");
        }

        // -------------------------------------------------------
        // TEST 6: Event envelope structure verification
        // -------------------------------------------------------
        @Test
        @DisplayName("6. EventEnvelope contains all required metadata fields")
        void eventEnvelope_shouldContainAllMetadata() throws Exception {
                // Given
                NotificationCreatedEvent payload = new NotificationCreatedEvent(
                                6L, "Metadata Test", "Check fields", "ALERT", "CRITICAL");

                EventEnvelope<NotificationCreatedEvent> envelope = new EventEnvelope<>(
                                EventTypes.NOTIFICATION_CREATED,
                                "Notification",
                                "6",
                                payload);

                // When
                domainEventPublisher.publish(envelope);
                OutboxEvent outboxEvent = outboxEventRepository.findAll().getFirst();
                String json = outboxEvent.getPayload();

                // Then - verify JSON structure
                var tree = objectMapper.readTree(json);
                assertTrue(tree.has("eventId"), "Envelope should contain eventId");
                assertTrue(tree.has("eventType"), "Envelope should contain eventType");
                assertTrue(tree.has("eventVersion"), "Envelope should contain eventVersion");
                assertTrue(tree.has("aggregateType"), "Envelope should contain aggregateType");
                assertTrue(tree.has("aggregateId"), "Envelope should contain aggregateId");
                assertTrue(tree.has("correlationId"), "Envelope should contain correlationId");
                assertTrue(tree.has("timestamp"), "Envelope should contain timestamp");
                assertTrue(tree.has("payload"), "Envelope should contain payload");

                assertEquals("ems.notification.created", tree.get("eventType").asText());
                assertEquals("1.0", tree.get("eventVersion").asText());
                assertEquals("Notification", tree.get("aggregateType").asText());
                assertEquals("6", tree.get("aggregateId").asText());

                // Verify inner payload
                var innerPayload = tree.get("payload");
                assertEquals(6, innerPayload.get("userId").asLong());
                assertEquals("Metadata Test", innerPayload.get("title").asText());

                System.out.println("✅ TEST 6 PASSED: EventEnvelope JSON structure verified with all metadata fields");
        }
}
