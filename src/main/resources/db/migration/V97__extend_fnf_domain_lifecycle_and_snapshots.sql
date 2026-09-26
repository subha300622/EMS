-- V97: Extend F&F Settlement Domain Lifecycle, Snapshots, and Payment Hardening

ALTER TABLE exit_fnf_settlements
    ADD COLUMN IF NOT EXISTS loan_recovery NUMERIC(15, 2) DEFAULT 0 CHECK (loan_recovery >= 0),
    ADD COLUMN IF NOT EXISTS snapshot_data JSONB,
    ADD COLUMN IF NOT EXISTS snapshot_version INTEGER DEFAULT 1,
    ADD COLUMN IF NOT EXISTS snapshot_hash VARCHAR(64),
    ADD COLUMN IF NOT EXISTS snapshot_created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT REFERENCES employees(id),
    ADD COLUMN IF NOT EXISTS finalized_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS finalized_by_id BIGINT REFERENCES employees(id),
    ADD COLUMN IF NOT EXISTS failure_reason TEXT,
    ADD COLUMN IF NOT EXISTS payment_attempts INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Ensure payment idempotency uniqueness per tenant organization
CREATE UNIQUE INDEX IF NOT EXISTS uq_exit_fnf_org_idempotency_key 
    ON exit_fnf_settlements (organization_id, idempotency_key) 
    WHERE idempotency_key IS NOT NULL;
