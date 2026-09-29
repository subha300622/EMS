-- ====================================================================
-- V100: Create Centralized Audit Logs Enhancements
-- Adds enterprise audit attributes, JSONB state capturing, and indexes
-- ====================================================================

ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS company_id BIGINT,
    ADD COLUMN IF NOT EXISTS department_id BIGINT,
    ADD COLUMN IF NOT EXISTS module VARCHAR(50),
    ADD COLUMN IF NOT EXISTS record_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS old_value JSONB,
    ADD COLUMN IF NOT EXISTS new_value JSONB,
    ADD COLUMN IF NOT EXISTS browser VARCHAR(100),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'SUCCESS',
    ADD COLUMN IF NOT EXISTS failure_reason TEXT,
    ADD COLUMN IF NOT EXISTS request_id VARCHAR(100);

-- Backfill legacy records
UPDATE audit_logs 
SET record_id = entity_id 
WHERE record_id IS NULL AND entity_id IS NOT NULL;

UPDATE audit_logs 
SET module = COALESCE(entity_type, 'SYSTEM') 
WHERE module IS NULL;

UPDATE audit_logs 
SET status = 'SUCCESS' 
WHERE status IS NULL;

-- Create performance indexes for centralized audit querying
CREATE INDEX IF NOT EXISTS idx_audit_logs_company_id ON audit_logs (company_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_module ON audit_logs (module);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_status ON audit_logs (status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_record ON audit_logs (entity_type, record_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_department_id ON audit_logs (department_id);
