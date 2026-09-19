-- ==============================================================================
-- Flyway Migration V89: Create Bonus Module Tables and Permissions
-- ==============================================================================

-- 1. Bonus Policies Table
CREATE TABLE IF NOT EXISTS bonus_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    bonus_type VARCHAR(50) NOT NULL,
    calculation_method VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    effective_from DATE NOT NULL,
    effective_to DATE,
    fixed_amount NUMERIC(15,2),
    percentage NUMERIC(7,4),
    minimum_amount NUMERIC(15,2),
    maximum_amount NUMERIC(15,2),
    target_value NUMERIC(15,2),
    minimum_rating NUMERIC(4,2),
    rating_slabs_json TEXT,
    formula_expression VARCHAR(500),
    department_id VARCHAR(50),
    designation_id VARCHAR(50),
    employee_type VARCHAR(50),
    branch_id VARCHAR(50),
    payment_frequency VARCHAR(50) NOT NULL DEFAULT 'YEARLY',
    approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    policy_version BIGINT NOT NULL DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bonus_policy_org_status ON bonus_policies(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_bonus_policy_org_dates ON bonus_policies(organization_id, effective_from, effective_to);

-- 2. Bonus Records Table
CREATE TABLE IF NOT EXISTS bonus_records (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    policy_id BIGINT NOT NULL REFERENCES bonus_policies(id) ON DELETE CASCADE,
    policy_version BIGINT NOT NULL DEFAULT 1,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    bonus_type VARCHAR(50) NOT NULL,
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
    payroll_posted_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bonus_record_org_emp_pol_period UNIQUE (organization_id, employee_id, policy_id, period_start, period_end)
);

CREATE INDEX IF NOT EXISTS idx_bonus_record_org_status ON bonus_records(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_bonus_record_org_payroll_status ON bonus_records(organization_id, payroll_status);
CREATE INDEX IF NOT EXISTS idx_bonus_record_org_period ON bonus_records(organization_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_bonus_record_org_emp ON bonus_records(organization_id, employee_id);

-- 3. Seed Permissions
INSERT INTO permissions (name, description)
VALUES
    ('BONUS_VIEW', 'Permission to view bonus policies and records'),
    ('BONUS_CREATE', 'Permission to calculate and create bonus records'),
    ('BONUS_ADJUST', 'Permission to adjust calculated bonus records'),
    ('BONUS_APPROVE', 'Permission to approve bonus records'),
    ('BONUS_POLICY_MANAGE', 'Permission to create, update, and manage bonus policies'),
    ('BONUS_PAYROLL_POST', 'Permission to post approved bonuses to payroll runs')
ON CONFLICT (name) DO NOTHING;

-- Map permissions to standard roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name IN ('BONUS_VIEW', 'BONUS_CREATE', 'BONUS_ADJUST', 'BONUS_APPROVE', 'BONUS_POLICY_MANAGE', 'BONUS_PAYROLL_POST')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'HR'
  AND p.name IN ('BONUS_VIEW', 'BONUS_CREATE', 'BONUS_ADJUST', 'BONUS_APPROVE', 'BONUS_POLICY_MANAGE', 'BONUS_PAYROLL_POST')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN ('BONUS_VIEW', 'BONUS_CREATE', 'BONUS_ADJUST', 'BONUS_APPROVE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
  AND p.name IN ('BONUS_VIEW')
ON CONFLICT DO NOTHING;
