-- Flyway Migration V94: Create Employee Exit, Multi-Department Clearance & F&F Settlement Module

-- 1. Employee Exits
CREATE TABLE IF NOT EXISTS employee_exits (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    exit_type VARCHAR(50) NOT NULL DEFAULT 'RESIGNATION',
    resignation_date DATE,
    requested_last_working_date DATE,
    last_working_date DATE,
    notice_period_days INT DEFAULT 30 CHECK (notice_period_days >= 0),
    notice_served_days INT DEFAULT 30 CHECK (notice_served_days >= 0),
    reason TEXT,
    remarks TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'MANAGER_APPROVAL_PENDING',
    reporting_manager_id BIGINT REFERENCES employees(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_employee_exits_org_status ON employee_exits(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_employee_exits_org_emp ON employee_exits(organization_id, employee_id);

-- 2. Exit Clearances with Assignment and Audit Separation
CREATE TABLE IF NOT EXISTS exit_clearances (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    exit_id BIGINT NOT NULL REFERENCES employee_exits(id) ON DELETE CASCADE,
    department VARCHAR(50) NOT NULL,
    assigned_to_id BIGINT NOT NULL REFERENCES employees(id),
    clearance_reason TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    cleared_by_id BIGINT REFERENCES employees(id),
    cleared_at TIMESTAMP WITH TIME ZONE,
    remarks TEXT,
    clearance_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exit_clearance_department UNIQUE (exit_id, department)
);

CREATE INDEX IF NOT EXISTS idx_exit_clearances_org_exit ON exit_clearances(organization_id, exit_id);
CREATE INDEX IF NOT EXISTS idx_exit_clearances_assigned ON exit_clearances(assigned_to_id, status);

-- 3. F&F Settlements with Tamper-Proof Constraints
CREATE TABLE IF NOT EXISTS exit_fnf_settlements (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    exit_id BIGINT NOT NULL REFERENCES employee_exits(id) ON DELETE CASCADE,
    salary_days_worked INT DEFAULT 0 CHECK (salary_days_worked >= 0),
    salary_amount NUMERIC(15, 2) DEFAULT 0 CHECK (salary_amount >= 0),
    unpaid_salary NUMERIC(15, 2) DEFAULT 0 CHECK (unpaid_salary >= 0),
    leave_encashment NUMERIC(15, 2) DEFAULT 0 CHECK (leave_encashment >= 0),
    bonus NUMERIC(15, 2) DEFAULT 0 CHECK (bonus >= 0),
    incentives NUMERIC(15, 2) DEFAULT 0 CHECK (incentives >= 0),
    overtime NUMERIC(15, 2) DEFAULT 0 CHECK (overtime >= 0),
    reimbursements NUMERIC(15, 2) DEFAULT 0 CHECK (reimbursements >= 0),
    gratuity NUMERIC(15, 2) DEFAULT 0 CHECK (gratuity >= 0),
    other_allowances NUMERIC(15, 2) DEFAULT 0 CHECK (other_allowances >= 0),
    total_earnings NUMERIC(15, 2) DEFAULT 0 CHECK (total_earnings >= 0),
    notice_period_recovery NUMERIC(15, 2) DEFAULT 0 CHECK (notice_period_recovery >= 0),
    asset_damage NUMERIC(15, 2) DEFAULT 0 CHECK (asset_damage >= 0),
    tax_deduction NUMERIC(15, 2) DEFAULT 0 CHECK (tax_deduction >= 0),
    other_deductions NUMERIC(15, 2) DEFAULT 0 CHECK (other_deductions >= 0),
    total_deductions NUMERIC(15, 2) DEFAULT 0 CHECK (total_deductions >= 0),
    net_settlement NUMERIC(15, 2) DEFAULT 0 CHECK (net_settlement >= 0),
    status VARCHAR(50) NOT NULL DEFAULT 'FINANCE_APPROVAL_PENDING',
    payment_method VARCHAR(50),
    payment_date DATE,
    payment_reference VARCHAR(100),
    payment_remarks TEXT,
    paid_amount NUMERIC(15, 2),
    paid_at TIMESTAMP WITH TIME ZONE,
    paid_by_id BIGINT REFERENCES employees(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exit_fnf_settlement_exit UNIQUE (exit_id)
);

CREATE INDEX IF NOT EXISTS idx_exit_fnf_settlements_org_status ON exit_fnf_settlements(organization_id, status);

-- 4. Enable Row Level Security (RLS)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'employee_exits') THEN
        ALTER TABLE employee_exits ENABLE ROW LEVEL SECURITY;
        ALTER TABLE employee_exits FORCE ROW LEVEL SECURITY;
        DROP POLICY IF EXISTS tenant_isolation_employee_exits_policy ON employee_exits;
        CREATE POLICY tenant_isolation_employee_exits_policy ON employee_exits FOR ALL
            USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
            WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'exit_clearances') THEN
        ALTER TABLE exit_clearances ENABLE ROW LEVEL SECURITY;
        ALTER TABLE exit_clearances FORCE ROW LEVEL SECURITY;
        DROP POLICY IF EXISTS tenant_isolation_exit_clearances_policy ON exit_clearances;
        CREATE POLICY tenant_isolation_exit_clearances_policy ON exit_clearances FOR ALL
            USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
            WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'exit_fnf_settlements') THEN
        ALTER TABLE exit_fnf_settlements ENABLE ROW LEVEL SECURITY;
        ALTER TABLE exit_fnf_settlements FORCE ROW LEVEL SECURITY;
        DROP POLICY IF EXISTS tenant_isolation_exit_fnf_settlements_policy ON exit_fnf_settlements;
        CREATE POLICY tenant_isolation_exit_fnf_settlements_policy ON exit_fnf_settlements FOR ALL
            USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
            WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');
    END IF;
END $$;

-- 5. Seed Permissions
INSERT INTO permissions (name, description) VALUES
('EXIT_CREATE', 'Permission to submit and create exit requests'),
('EXIT_VIEW', 'Permission to view exit requests and offboarding status'),
('EXIT_APPROVE', 'Permission to approve exit requests'),
('EXIT_REJECT', 'Permission to reject exit requests'),
('CLEARANCE_MANAGE', 'Permission to assign and manage clearances'),
('CLEARANCE_ACTION', 'Permission to clear, hold or reject clearance tasks'),
('FNF_CALCULATE', 'Permission to compute F&F settlements'),
('FNF_VIEW', 'Permission to view F&F settlements and breakdowns'),
('FNF_APPROVE', 'Permission to approve F&F settlements'),
('FNF_PAYMENT', 'Permission to release F&F payments'),
('FNF_REPORT_VIEW', 'Permission to view F&F reports and analytics')
ON CONFLICT (name) DO NOTHING;
