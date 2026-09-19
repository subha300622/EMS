-- V86__attendance_grace_and_permissions.sql
-- Attendance Grace Time, Grace Usage Tracking, and Attendance Permissions / Adjustment Controls

-- 1. Extend Attendance Policies with Grace & Permission Quota Settings
ALTER TABLE attendance_policies
    ADD COLUMN IF NOT EXISTS late_grace_minutes INT NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS early_exit_grace_minutes INT NOT NULL DEFAULT 10,
    ADD COLUMN IF NOT EXISTS grace_occurrences_per_period INT NOT NULL DEFAULT 3,
    ADD COLUMN IF NOT EXISTS grace_period_type VARCHAR(30) NOT NULL DEFAULT 'MONTHLY',
    ADD COLUMN IF NOT EXISTS allow_late_grace BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS allow_early_exit_grace BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS exceed_grace_action VARCHAR(30) NOT NULL DEFAULT 'MARK_LATE',
    ADD COLUMN IF NOT EXISTS max_monthly_permissions INT NOT NULL DEFAULT 4,
    ADD COLUMN IF NOT EXISTS max_daily_permission_minutes INT NOT NULL DEFAULT 120,
    ADD COLUMN IF NOT EXISTS max_monthly_permission_minutes INT NOT NULL DEFAULT 480;

-- 2. Create Attendance Grace Usage Table
CREATE TABLE IF NOT EXISTS attendance_grace_usage (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    grace_type VARCHAR(30) NOT NULL, -- 'LATE_ARRIVAL', 'EARLY_EXIT'
    grace_minutes_used INT NOT NULL DEFAULT 0,
    within_grace BOOLEAN NOT NULL DEFAULT TRUE,
    policy_id BIGINT REFERENCES attendance_policies(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_att_grace_usage_org_emp_date 
    ON attendance_grace_usage (organization_id, employee_id, attendance_date);

CREATE INDEX IF NOT EXISTS idx_att_grace_usage_period_lookup 
    ON attendance_grace_usage (organization_id, employee_id, grace_type, attendance_date);

-- 3. Create Attendance Permissions Table
CREATE TABLE IF NOT EXISTS attendance_permissions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    permission_type VARCHAR(50) NOT NULL, -- 'LATE_ARRIVAL', 'EARLY_EXIT', 'MISSING_PUNCH', etc.
    requested_minutes INT,
    expected_time TIME WITHOUT TIME ZONE,
    actual_time TIME WITHOUT TIME ZONE,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED', 'APPLIED', 'CANCELLED'
    rejection_reason TEXT,
    workflow_instance_id VARCHAR(100),
    approved_by VARCHAR(100),
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_att_perm_org_emp_date 
    ON attendance_permissions (organization_id, employee_id, attendance_date);

CREATE INDEX IF NOT EXISTS idx_att_perm_org_status 
    ON attendance_permissions (organization_id, status);

CREATE INDEX IF NOT EXISTS idx_att_perm_wf_instance 
    ON attendance_permissions (workflow_instance_id);

-- 4. Extend Attendance Table with Normalized Calculation & Status Fields
ALTER TABLE attendance
    ADD COLUMN IF NOT EXISTS late_status VARCHAR(30) NOT NULL DEFAULT 'NONE', -- 'NONE', 'GRACE_APPLIED', 'EXCUSED', 'UNEXCUSED'
    ADD COLUMN IF NOT EXISTS early_exit_status VARCHAR(30) NOT NULL DEFAULT 'NONE', -- 'NONE', 'GRACE_APPLIED', 'EXCUSED', 'UNEXCUSED'
    ADD COLUMN IF NOT EXISTS grace_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS permission_minutes INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS payable_minutes INT NOT NULL DEFAULT 0;
