-- =============================================================================
-- Flyway Migration: V95__harden_exit_and_fnf_constraints.sql
-- Adversarial Hardening for Employee Exit & F&F Settlement Module:
-- 1. Adds idempotency_key column to exit_fnf_settlements.
-- 2. Enforces database uniqueness on (organization_id, idempotency_key).
-- 3. Enforces database uniqueness on (organization_id, payment_reference).
-- 4. Enforces non-negative salary days worked check constraint.
-- =============================================================================

-- 1. Add idempotency_key column
ALTER TABLE exit_fnf_settlements 
ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);

-- 2. Partial unique index for idempotency_key per tenant
CREATE UNIQUE INDEX IF NOT EXISTS uq_exit_fnf_idempotency_key
ON exit_fnf_settlements(organization_id, idempotency_key)
WHERE idempotency_key IS NOT NULL;

-- 3. Partial unique index for payment_reference (UTR/Bank transaction) per tenant
CREATE UNIQUE INDEX IF NOT EXISTS uq_exit_fnf_payment_reference
ON exit_fnf_settlements(organization_id, payment_reference)
WHERE payment_reference IS NOT NULL;

-- 4. Ensure non-negative salary days worked constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_exit_fnf_salary_days_positive'
    ) THEN
        ALTER TABLE exit_fnf_settlements
        ADD CONSTRAINT chk_exit_fnf_salary_days_positive CHECK (salary_days_worked >= 0);
    END IF;
END $$;
