-- ============================================================================
-- Flyway Migration V87: Overtime (OT) Module Schema
-- Creates overtime_policies and overtime_records tables with tenant isolation,
-- optimistic locking, audit capabilities, and idempotency indexes.
-- ============================================================================

-- 1. Overtime Policies Table
CREATE TABLE IF NOT EXISTS overtime_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    effective_from DATE NOT NULL,
    effective_to DATE,
    normal_working_hours INTEGER NOT NULL DEFAULT 8,
    minimum_ot_minutes INTEGER NOT NULL DEFAULT 30,
    maximum_ot_minutes INTEGER NOT NULL DEFAULT 240,
    amount_basis VARCHAR(30) NOT NULL DEFAULT 'BASIC_SALARY',
    fixed_hourly_rate NUMERIC(15, 2),
    working_days_per_month INTEGER NOT NULL DEFAULT 26,
    working_hours_per_day INTEGER NOT NULL DEFAULT 8,
    normal_day_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 1.50,
    weekend_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 2.00,
    holiday_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 2.00,
    rounding_rule VARCHAR(30) NOT NULL DEFAULT 'EXACT',
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    department_id VARCHAR(50),
    designation_id VARCHAR(50),
    employee_type VARCHAR(50),
    branch_id VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ot_policy_org_status ON overtime_policies (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_ot_policy_org_effective ON overtime_policies (organization_id, effective_from, effective_to);

-- 2. Overtime Records Table
CREATE TABLE IF NOT EXISTS overtime_records (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    attendance_id BIGINT REFERENCES attendance(id) ON DELETE SET NULL,
    policy_id BIGINT REFERENCES overtime_policies(id) ON DELETE SET NULL,
    policy_version BIGINT NOT NULL DEFAULT 0,
    work_date DATE NOT NULL,
    scheduled_minutes INTEGER NOT NULL DEFAULT 480,
    worked_minutes INTEGER NOT NULL DEFAULT 0,
    raw_ot_minutes INTEGER NOT NULL DEFAULT 0,
    calculated_ot_minutes INTEGER NOT NULL DEFAULT 0,
    adjusted_ot_minutes INTEGER,
    approved_ot_minutes INTEGER,
    amount_basis VARCHAR(30) NOT NULL,
    hourly_rate NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    ot_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 1.50,
    ot_rate NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    calculated_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    adjusted_amount NUMERIC(15, 2),
    approved_amount NUMERIC(15, 2),
    day_type VARCHAR(30) NOT NULL DEFAULT 'NORMAL_DAY',
    status VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',
    adjustment_reason TEXT,
    adjusted_by VARCHAR(100),
    adjusted_at TIMESTAMP,
    approved_by VARCHAR(100),
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    rejected_by VARCHAR(100),
    rejected_at TIMESTAMP,
    workflow_instance_id VARCHAR(100),
    payroll_run_id BIGINT,
    payroll_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ot_record_org_emp_att UNIQUE (organization_id, employee_id, attendance_id)
);

CREATE INDEX IF NOT EXISTS idx_ot_record_org_emp_date ON overtime_records (organization_id, employee_id, work_date);
CREATE INDEX IF NOT EXISTS idx_ot_record_org_status ON overtime_records (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_ot_record_org_payroll ON overtime_records (organization_id, payroll_status, work_date);
CREATE INDEX IF NOT EXISTS idx_ot_record_workflow ON overtime_records (workflow_instance_id);

-- 3. Seed Overtime Master Permissions
INSERT INTO permissions (name, description) VALUES
('OVERTIME_VIEW', 'Permission to view overtime records and policies'),
('OVERTIME_CREATE', 'Permission to create and calculate overtime records'),
('OVERTIME_ADJUST', 'Permission to adjust calculated overtime records'),
('OVERTIME_APPROVE', 'Permission to approve or reject overtime workflow requests'),
('OVERTIME_POLICY_MANAGE', 'Permission to create, update, and manage overtime policies'),
('OVERTIME_PAYROLL_POST', 'Permission to post approved overtime to payroll variable earnings')
ON CONFLICT (name) DO NOTHING;
