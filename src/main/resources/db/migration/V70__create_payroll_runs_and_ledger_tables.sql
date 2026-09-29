-- Flyway Migration V70: Payroll Runs and Historical Ledger Snapshots

CREATE TABLE IF NOT EXISTS payroll_runs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_employees INT NOT NULL DEFAULT 0,
    processed_employees INT NOT NULL DEFAULT 0,
    total_gross NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_benefits NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_deductions NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    total_net NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    created_by BIGINT,
    updated_by BIGINT,
    finalized_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_payroll_run_period UNIQUE (organization_id, period_start, period_end),
    CONSTRAINT fk_payroll_run_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payroll_employees (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    payroll_run_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(255) NOT NULL,
    employee_code VARCHAR(100),
    gross_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    benefits_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    deductions_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'CALCULATED',
    calculation_date DATE NOT NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_payroll_employee UNIQUE (payroll_run_id, employee_id),
    CONSTRAINT fk_payroll_employee_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_employee_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS payroll_items (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    payroll_employee_id BIGINT NOT NULL,
    salary_component_id BIGINT,
    component_code VARCHAR(50) NOT NULL,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(30) NOT NULL,
    calculation_type VARCHAR(30) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    percentage NUMERIC(7, 4),
    calculation_base VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payroll_item_emp FOREIGN KEY (payroll_employee_id) REFERENCES payroll_employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_payroll_runs_org ON payroll_runs(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_payroll_employees_run ON payroll_employees(payroll_run_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_payroll_employees_emp ON payroll_employees(employee_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_payroll_items_emp ON payroll_items(payroll_employee_id);
