-- V84: Leave Business Logic Refinement
-- 1. Employee Leave Policy Assignments join table
CREATE TABLE IF NOT EXISTS public.employee_leave_policies (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_policy_id BIGINT NOT NULL,
    organization_id BIGINT,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_emp_leave_policy UNIQUE (employee_id, leave_policy_id)
);

CREATE INDEX IF NOT EXISTS idx_emp_leave_policy_emp ON public.employee_leave_policies(employee_id);
CREATE INDEX IF NOT EXISTS idx_emp_leave_policy_pol ON public.employee_leave_policies(leave_policy_id);
CREATE INDEX IF NOT EXISTS idx_emp_leave_policy_org ON public.employee_leave_policies(organization_id);

-- 2. Add paid_days and lop_days to leaves table
ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS paid_days DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE public.leaves ADD COLUMN IF NOT EXISTS lop_days DOUBLE PRECISION DEFAULT 0.0;

-- 3. Add LOP and encashment rule constraints to leave_rules table
ALTER TABLE public.leave_rules ADD COLUMN IF NOT EXISTS allow_lop BOOLEAN DEFAULT TRUE;
ALTER TABLE public.leave_rules ADD COLUMN IF NOT EXISTS allow_encashment BOOLEAN DEFAULT FALSE;
ALTER TABLE public.leave_rules ADD COLUMN IF NOT EXISTS max_encashment_days DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE public.leave_rules ADD COLUMN IF NOT EXISTS min_balance_retained DOUBLE PRECISION DEFAULT 0.0;

-- 4. Unique constraint on leave_accrual_transactions to guarantee accrual idempotency at DB level
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_leave_accrual_emp_type_period'
    ) THEN
        ALTER TABLE public.leave_accrual_transactions 
        ADD CONSTRAINT uk_leave_accrual_emp_type_period UNIQUE (employee_id, leave_type_id, period);
    END IF;
END $$;
