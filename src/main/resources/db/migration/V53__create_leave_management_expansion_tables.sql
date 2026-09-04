-- V53: Create Leave Management Expansion Tables & Columns

ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS duration_type VARCHAR(50) DEFAULT 'FULL_DAY';
ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS duration_days DOUBLE PRECISION DEFAULT 1.0;
ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS approval_workflow_instance_id VARCHAR(255);

ALTER TABLE public.leave_policies ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.leave_types ADD COLUMN IF NOT EXISTS organization_id BIGINT;

CREATE TABLE IF NOT EXISTS public.leave_rules (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    leave_type_id BIGINT,
    min_service_days INT DEFAULT 0,
    max_consecutive_days INT DEFAULT 14,
    include_weekends BOOLEAN DEFAULT FALSE,
    include_holidays BOOLEAN DEFAULT FALSE,
    allow_half_day BOOLEAN DEFAULT TRUE,
    notice_period_days INT DEFAULT 0,
    allow_negative_balance BOOLEAN DEFAULT FALSE,
    max_carry_forward_days INT DEFAULT 5,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.leave_accrual_rules (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    leave_type_id BIGINT,
    annual_quota INT DEFAULT 12,
    accrual_frequency VARCHAR(50) DEFAULT 'MONTHLY',
    credit_amount DOUBLE PRECISION DEFAULT 1.0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    organization_id BIGINT,
    total_entitlement DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    used_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    pending_balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    year INT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_leave_balances_emp_type_year UNIQUE (employee_id, leave_type_id, year)
);

CREATE TABLE IF NOT EXISTS public.leave_accrual_transactions (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    organization_id BIGINT,
    accrued_amount DOUBLE PRECISION NOT NULL,
    period VARCHAR(50),
    accrued_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.leave_balance_adjustments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    organization_id BIGINT,
    adjustment_amount DOUBLE PRECISION NOT NULL,
    reason TEXT,
    adjusted_by_id BIGINT,
    adjusted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.leave_encashments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    organization_id BIGINT,
    days_encashed DOUBLE PRECISION NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.leave_request_histories (
    id BIGSERIAL PRIMARY KEY,
    leave_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by_id BIGINT,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    remarks TEXT
);

CREATE INDEX IF NOT EXISTS idx_leave_balances_emp ON public.leave_balances(employee_id, organization_id);
CREATE INDEX IF NOT EXISTS idx_leave_accrual_tx_emp ON public.leave_accrual_transactions(employee_id);
CREATE INDEX IF NOT EXISTS idx_leave_request_histories_leave ON public.leave_request_histories(leave_id);
