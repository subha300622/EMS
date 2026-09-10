-- =============================================================================
-- Migration V81: Attendance Core Lifecycle, Breaks, Versioning, and Isolation
-- =============================================================================

DO $$
BEGIN
    -- Add organization_id
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='organization_id') THEN
        ALTER TABLE public.attendance ADD COLUMN organization_id bigint REFERENCES public.organizations(id);
    END IF;

    -- Add total_break_minutes
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='total_break_minutes') THEN
        ALTER TABLE public.attendance ADD COLUMN total_break_minutes integer DEFAULT 0;
    END IF;

    -- Add total_working_minutes
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='total_working_minutes') THEN
        ALTER TABLE public.attendance ADD COLUMN total_working_minutes integer;
    END IF;

    -- Add check_in_time (timestamp with time zone)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='check_in_time') THEN
        ALTER TABLE public.attendance ADD COLUMN check_in_time timestamp with time zone;
    END IF;

    -- Add check_out_time (timestamp with time zone)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='check_out_time') THEN
        ALTER TABLE public.attendance ADD COLUMN check_out_time timestamp with time zone;
    END IF;

    -- Add version for optimistic concurrency locking
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='version') THEN
        ALTER TABLE public.attendance ADD COLUMN version bigint DEFAULT 0 NOT NULL;
    END IF;

    -- Add created_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='created_at') THEN
        ALTER TABLE public.attendance ADD COLUMN created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP;
    END IF;

    -- Add updated_at
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='updated_at') THEN
        ALTER TABLE public.attendance ADD COLUMN updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Backfill organization_id from employees table
UPDATE public.attendance a
SET organization_id = e.organization_id
FROM public.employees e
WHERE a.employee_id = e.id AND a.organization_id IS NULL AND e.organization_id IS NOT NULL;

-- Create attendance_breaks table
CREATE TABLE IF NOT EXISTS public.attendance_breaks (
    id bigserial PRIMARY KEY,
    attendance_id bigint NOT NULL REFERENCES public.attendance(id) ON DELETE CASCADE,
    organization_id bigint REFERENCES public.organizations(id),
    break_start_time timestamp with time zone NOT NULL,
    break_end_time timestamp with time zone,
    duration_minutes integer,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);

-- Indices for fast lookups and break query scoping
CREATE INDEX IF NOT EXISTS idx_attendance_breaks_att_id ON public.attendance_breaks (attendance_id);
CREATE INDEX IF NOT EXISTS idx_attendance_breaks_open ON public.attendance_breaks (attendance_id) WHERE break_end_time IS NULL;
CREATE INDEX IF NOT EXISTS idx_attendance_breaks_org_id ON public.attendance_breaks (organization_id);

-- Enforce UNIQUE constraint on (organization_id, employee_id, date)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_attendance_org_employee_date'
    ) THEN
        -- Safely drop single employee_date constraint if replacing or keep as fallback
        ALTER TABLE public.attendance
            ADD CONSTRAINT uk_attendance_org_employee_date UNIQUE (organization_id, employee_id, date);
    END IF;
EXCEPTION
    WHEN duplicate_table THEN
        NULL;
    WHEN duplicate_object THEN
        NULL;
END $$;

-- Enable PostgreSQL Row Level Security (RLS) on attendance_breaks
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'attendance_breaks') THEN
        ALTER TABLE public.attendance_breaks ENABLE ROW LEVEL SECURITY;
        ALTER TABLE public.attendance_breaks FORCE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS tenant_isolation_attendance_breaks_policy ON public.attendance_breaks;
        CREATE POLICY tenant_isolation_attendance_breaks_policy ON public.attendance_breaks
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            );
    END IF;
END $$;
