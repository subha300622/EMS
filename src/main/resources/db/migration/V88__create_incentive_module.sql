-- ==============================================================================
-- Flyway Migration V88: Create Incentive Module Tables and Permissions
-- ==============================================================================

-- 1. Incentive Policies Table
CREATE TABLE IF NOT EXISTS incentive_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    incentive_type VARCHAR(50) NOT NULL,
    calculation_method VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    effective_from DATE NOT NULL,
    effective_to DATE,
    fixed_amount NUMERIC(15,2),
    percentage NUMERIC(7,4),
    minimum_amount NUMERIC(15,2),
    maximum_amount NUMERIC(15,2),
    target_value NUMERIC(15,2),
    minimum_achievement_percentage NUMERIC(7,2),
    minimum_rating NUMERIC(4,2),
    target_slabs_json TEXT,
    formula_expression VARCHAR(500),
    department_id VARCHAR(50),
    designation_id VARCHAR(50),
    employee_type VARCHAR(50),
    branch_id VARCHAR(50),
    payment_frequency VARCHAR(50) NOT NULL DEFAULT 'MONTHLY',
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    policy_version BIGINT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inc_policy_org_status ON incentive_policies(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_inc_policy_org_dates ON incentive_policies(organization_id, effective_from, effective_to);

-- 2. Incentive Records Table
CREATE TABLE IF NOT EXISTS incentive_records (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    policy_id BIGINT NOT NULL REFERENCES incentive_policies(id) ON DELETE CASCADE,
    policy_version BIGINT NOT NULL DEFAULT 1,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    incentive_type VARCHAR(50) NOT NULL,
    calculation_method VARCHAR(50) NOT NULL,
    target_value NUMERIC(15,2),
    achieved_value NUMERIC(15,2),
    achievement_percentage NUMERIC(7,2),
    performance_rating NUMERIC(4,2),
    calculated_amount NUMERIC(15,2) NOT NULL,
    adjusted_amount NUMERIC(15,2),
    approved_amount NUMERIC(15,2),
    adjustment_reason TEXT,
    adjusted_by VARCHAR(255),
    adjusted_at TIMESTAMP WITHOUT TIME ZONE,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP WITHOUT TIME ZONE,
    rejection_reason TEXT,
    rejected_by VARCHAR(255),
    rejected_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'CALCULATED',
    workflow_instance_id VARCHAR(100),
    payroll_run_id BIGINT,
    payroll_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inc_record_org_emp_pol_period UNIQUE (organization_id, employee_id, policy_id, period_start, period_end)
);

CREATE INDEX IF NOT EXISTS idx_inc_record_org_status ON incentive_records(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_inc_record_org_payroll_status ON incentive_records(organization_id, payroll_status);
CREATE INDEX IF NOT EXISTS idx_inc_record_org_period ON incentive_records(organization_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_inc_record_org_emp ON incentive_records(organization_id, employee_id);

-- 3. Seed Permissions
INSERT INTO permissions (name, description)
VALUES
    ('INCENTIVE_VIEW', 'Permission to view incentive policies and records'),
    ('INCENTIVE_CREATE', 'Permission to calculate and create incentive records'),
    ('INCENTIVE_ADJUST', 'Permission to adjust calculated incentive records'),
    ('INCENTIVE_APPROVE', 'Permission to approve incentive records'),
    ('INCENTIVE_POLICY_MANAGE', 'Permission to create, update, and manage incentive policies'),
    ('INCENTIVE_PAYROLL_POST', 'Permission to post approved incentives to payroll runs')
ON CONFLICT (name) DO NOTHING;

-- Map permissions to default roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name IN ('INCENTIVE_VIEW', 'INCENTIVE_CREATE', 'INCENTIVE_ADJUST', 'INCENTIVE_APPROVE', 'INCENTIVE_POLICY_MANAGE', 'INCENTIVE_PAYROLL_POST')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'HR'
  AND p.name IN ('INCENTIVE_VIEW', 'INCENTIVE_CREATE', 'INCENTIVE_ADJUST', 'INCENTIVE_APPROVE', 'INCENTIVE_POLICY_MANAGE', 'INCENTIVE_PAYROLL_POST')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN ('INCENTIVE_VIEW', 'INCENTIVE_CREATE', 'INCENTIVE_ADJUST')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
  AND p.name IN ('INCENTIVE_VIEW')
ON CONFLICT DO NOTHING;
