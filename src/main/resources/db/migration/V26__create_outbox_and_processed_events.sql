-- Outbox Events table for Transactional Outbox Pattern
CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(200) NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INT          NOT NULL DEFAULT 0,
    max_retries     INT          NOT NULL DEFAULT 5,
    last_error      TEXT,
    event_version   VARCHAR(20)  NOT NULL DEFAULT '1.0',
    tenant_id       BIGINT,
    correlation_id  VARCHAR(100),
    causation_id    VARCHAR(100),
    partition_key   VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP
);

-- Partial index for efficient polling of unprocessed events
CREATE INDEX idx_outbox_unprocessed
    ON outbox_events (created_at ASC)
    WHERE status IN ('PENDING', 'FAILED');

-- Processed Events table for consumer idempotency
-- Composite PK allows the same event to be tracked per consumer group
DROP TABLE IF EXISTS processed_events CASCADE;
CREATE TABLE processed_events (
    event_id       UUID         NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    processed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id, consumer_group)
);
