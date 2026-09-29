-- ====================================================================
-- V101: Add Permission and HTTP Context to Centralized Audit Logs
-- Supports permission-driven, role-agnostic audit records
-- ====================================================================

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS permission VARCHAR(100),
    ADD COLUMN IF NOT EXISTS http_method VARCHAR(10),
    ADD COLUMN IF NOT EXISTS api_path VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_audit_logs_permission ON audit_logs (permission);
