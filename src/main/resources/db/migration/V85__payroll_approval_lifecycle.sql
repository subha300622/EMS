-- Flyway Migration V85: Add Approval and Lifecycle fields to payroll_runs

ALTER TABLE payroll_runs
    ADD COLUMN IF NOT EXISTS approval_instance_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS approved_by BIGINT,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_payroll_runs_approval_instance ON payroll_runs(approval_instance_id);
