-- =============================================================================
-- Migration V83: Attendance Policies, Adjustments, and Early/Late Fields
-- =============================================================================

-- 1. Create attendance_policies table
CREATE TABLE IF NOT EXISTS public.attendance_policies (
    id bigserial PRIMARY KEY,
    organization_id bigint NOT NULL REFERENCES public.organizations(id),
    name varchar(150) NOT NULL,
    office_start_time time without time zone NOT NULL,
    office_end_time time without time zone NOT NULL,
    grace_period_minutes integer NOT NULL DEFAULT 15,
    minimum_working_minutes integer NOT NULL DEFAULT 480,
    half_day_threshold integer NOT NULL DEFAULT 240,
    late_threshold integer NOT NULL DEFAULT 15,
    early_checkout_threshold integer NOT NULL DEFAULT 15,
    maximum_break_minutes integer NOT NULL DEFAULT 60,
    status varchar(30) NOT NULL DEFAULT 'DRAFT',
    version bigint NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_att_policy_org_status ON public.attendance_policies (organization_id, status);

-- 2. Create attendance_adjustments table
CREATE TABLE IF NOT EXISTS public.attendance_adjustments (
    id bigserial PRIMARY KEY,
    organization_id bigint NOT NULL REFERENCES public.organizations(id),
    attendance_id bigint NOT NULL REFERENCES public.attendance(id) ON DELETE CASCADE,
    employee_id bigint NOT NULL REFERENCES public.employees(id) ON DELETE CASCADE,
    requested_check_in_time timestamp with time zone,
    requested_check_out_time timestamp with time zone,
    reason text NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'PENDING',
    workflow_instance_id varchar(100),
    approved_by varchar(150),
    approved_at timestamp with time zone,
    rejection_reason text,
    manager_notes text,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_att_adj_org_emp ON public.attendance_adjustments (organization_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_att_adj_org_status ON public.attendance_adjustments (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_att_adj_attendance ON public.attendance_adjustments (attendance_id);

-- 3. Add early checkout and late minutes fields to attendance table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='is_early_checkout') THEN
        ALTER TABLE public.attendance ADD COLUMN is_early_checkout boolean DEFAULT false;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='early_by') THEN
        ALTER TABLE public.attendance ADD COLUMN early_by varchar(20) DEFAULT '00:00';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='late_by_minutes') THEN
        ALTER TABLE public.attendance ADD COLUMN late_by_minutes integer DEFAULT 0;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='early_by_minutes') THEN
        ALTER TABLE public.attendance ADD COLUMN early_by_minutes integer DEFAULT 0;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='is_half_day') THEN
        ALTER TABLE public.attendance ADD COLUMN is_half_day boolean DEFAULT false;
    END IF;
END $$;
