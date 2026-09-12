-- Migration: V82__attendance_regularization_improvements.sql
-- Purpose: Enhance attendance_regularizations with tenant scoping, attendance linkage, workflow metadata, and unique pending constraint.

ALTER TABLE attendance_regularizations
    ADD COLUMN IF NOT EXISTS attendance_id BIGINT,
    ADD COLUMN IF NOT EXISTS organization_id BIGINT,
    ADD COLUMN IF NOT EXISTS requested_check_in_time TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS requested_check_out_time TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS workflow_instance_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS approved_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP;

-- Add Foreign Key Constraints if they do not exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_attendance_reg_attendance'
    ) THEN
        ALTER TABLE attendance_regularizations
            ADD CONSTRAINT fk_attendance_reg_attendance
            FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_attendance_reg_organization'
    ) THEN
        ALTER TABLE attendance_regularizations
            ADD CONSTRAINT fk_attendance_reg_organization
            FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Indexes for tenant and employee queries
CREATE INDEX IF NOT EXISTS idx_attendance_reg_org_emp
    ON attendance_regularizations (organization_id, employee_id, status);

CREATE INDEX IF NOT EXISTS idx_attendance_reg_workflow
    ON attendance_regularizations (workflow_instance_id);

-- Concurrency protection: At most one active PENDING regularization per attendance session
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_reg_pending
    ON attendance_regularizations (attendance_id)
    WHERE status = 'PENDING';
