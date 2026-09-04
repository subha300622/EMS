# Kafka Hybrid Event-Driven Analytics Pipeline Blueprint

This document outlines the blueprint for scaling our Stripe-grade billing analytics subsystem into a distributed, event-driven streaming model using Apache Kafka.

---

## 🏗 End-to-End Ingestion Flow

```text
       Billing Checkout / API Action
                     ↓
        PostgreSQL OLTP Databases
       (Writes event to ledger & outbox)
                     ↓
             Transaction Commit
                     ↓
       Outbox Poller (CDC or Debezium)
                     ↓
                Kafka Cluster
         (Topic: billing-telemetry-events)
                     ↓
         Materializer Consumer Group
         (Ordered execution per shard)
                     ↓
          Derived Projection Tables
       (subscription_metrics_daily)
```

---

## 🔐 Guaranteed Correctness & Inconsistency Mitigation

### 1. Dual-Write Elimination via Transactional Outbox
To prevent ghost events and out-of-sync states under system crashes, we avoid calling both Kafka and PostgreSQL inside the main application code (dual-writing). Instead, we enforce the **Transactional Outbox Pattern**:
- When a payment is processed, the write transaction commits:
  1. Primary transactional domain changes (e.g. mark invoice PAID).
  2. Append event to `analytics_event_log` ledger.
  3. Write event to `outbox_events` publishing queue.
- An outbox polling worker (or Debezium CDC broker) reads the transactional outbox and publishes the messages to Kafka. This guarantees **At-Least-Once Delivery** to Kafka.

### 2. Message Ordering via Partition Keys
To ensure updates are processed in order and avoid race conditions (e.g. processing `PaymentSucceeded` before `SubscriptionCreated` has arrived):
- We define `subscription_id` or `organization_id` as the **Kafka Partition Key**.
- Kafka routes all events for the same subscription to the **same partition**, which ensures they are processed sequentially by the consumer.

### 3. Idempotent Consumer Aggregation
When the consumer processes an event, it executes:
```sql
INSERT INTO analytics_payment_facts (event_global_sequence, subscription_id, amount, status, plan_code, billing_cycle, event_id, created_at)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (event_id) DO NOTHING;
```
This DB constraint guarantees **Exactly-Once Semantics (Logical)** at the datastore level, ignoring any duplicated deliveries from Kafka retry loops.

### 4. Consumer Group Offset Checkpointing
- Offset commits are managed by Spring Kafka using `AckMode.MANUAL_IMMEDIATE`.
- A consumer offset is only committed **AFTER** the transaction writing the fact database changes successfully commits, ensuring zero message loss on worker failure.
