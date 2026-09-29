-- Flyway Migration V71: RazorpayX Payment Configurations, Employee Bank Accounts, Payouts, and Webhooks

CREATE TABLE IF NOT EXISTS organization_payment_configs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAYX',
    environment VARCHAR(30) NOT NULL DEFAULT 'TEST',
    api_key VARCHAR(255),
    api_secret VARCHAR(255),
    account_number VARCHAR(100),
    webhook_secret VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_config_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employee_payment_accounts (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL UNIQUE,
    provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAYX',
    contact_id VARCHAR(100),
    fund_account_id VARCHAR(100),
    account_type VARCHAR(30) NOT NULL DEFAULT 'BANK_ACCOUNT',
    account_number VARCHAR(50),
    ifsc_code VARCHAR(30),
    beneficiary_name VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_employee_payment_acc_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payroll_payments (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    payroll_run_id BIGINT NOT NULL,
    payroll_employee_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    payout_id VARCHAR(100),
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    utr VARCHAR(100),
    provider VARCHAR(30) NOT NULL DEFAULT 'RAZORPAYX',
    mode VARCHAR(30) NOT NULL DEFAULT 'NEFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payroll_payment_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_payment_emp FOREIGN KEY (payroll_employee_id) REFERENCES payroll_employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_payment_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS razorpayx_webhook_events (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    payload TEXT NOT NULL,
    signature VARCHAR(255),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payroll_payments_run ON payroll_payments(payroll_run_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_emp ON payroll_payments(employee_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_payroll_payments_payout ON payroll_payments(payout_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_entity ON razorpayx_webhook_events(entity_id);
