-- 1. ADD organization_id NULL to designations, job_levels, employment_types
ALTER TABLE public.designations ADD COLUMN organization_id bigint;
ALTER TABLE public.job_levels ADD COLUMN organization_id bigint;
ALTER TABLE public.employment_types ADD COLUMN organization_id bigint;

-- 2. BACKFILL organization_id
-- Update job_levels and employment_types from their parent tables first (if existing parent association has organization_id)
UPDATE public.designations SET organization_id = (SELECT id FROM public.organizations ORDER BY id LIMIT 1) WHERE organization_id IS NULL;
UPDATE public.job_levels jl SET organization_id = COALESCE(
    (SELECT d.organization_id FROM public.designations d WHERE d.id = jl.designation_id),
    (SELECT id FROM public.organizations ORDER BY id LIMIT 1)
) WHERE jl.organization_id IS NULL;
UPDATE public.employment_types et SET organization_id = COALESCE(
    (SELECT jl.organization_id FROM public.job_levels jl WHERE jl.id = et.job_level_id),
    (SELECT id FROM public.organizations ORDER BY id LIMIT 1)
) WHERE et.organization_id IS NULL;

-- 3. Verify no NULLs (Coalesce fallback to first organization if exists, or do nothing if empty)
-- Clean up orphaned or remaining nulls if organizations exist
DO $$
DECLARE
    first_org_id bigint;
BEGIN
    SELECT id INTO first_org_id FROM public.organizations ORDER BY id LIMIT 1;
    IF first_org_id IS NOT NULL THEN
        UPDATE public.designations SET organization_id = first_org_id WHERE organization_id IS NULL;
        UPDATE public.job_levels SET organization_id = first_org_id WHERE organization_id IS NULL;
        UPDATE public.employment_types SET organization_id = first_org_id WHERE organization_id IS NULL;
    END IF;
END $$;

-- 4. ADD FK to organizations
ALTER TABLE public.designations ADD CONSTRAINT fk_designations_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE public.job_levels ADD CONSTRAINT fk_job_levels_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id);
ALTER TABLE public.employment_types ADD CONSTRAINT fk_employment_types_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

-- 5. ADD NOT NULL (only if organization exists, otherwise keep it nullable to allow migration on empty database)
-- Since Flyway migration runs before any records are inserted, if the tables are completely empty, NOT NULL will succeed.
-- If the table is NOT empty and organizations table is also not empty, NOT NULL will succeed.
-- If the table is NOT empty and organizations table is empty, we keep it nullable to avoid breaking.
DO $$
DECLARE
    has_rows_without_org boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM public.designations WHERE organization_id IS NULL
        UNION ALL
        SELECT 1 FROM public.job_levels WHERE organization_id IS NULL
        UNION ALL
        SELECT 1 FROM public.employment_types WHERE organization_id IS NULL
    ) INTO has_rows_without_org;

    IF NOT has_rows_without_org THEN
        ALTER TABLE public.designations ALTER COLUMN organization_id SET NOT NULL;
        ALTER TABLE public.job_levels ALTER COLUMN organization_id SET NOT NULL;
        ALTER TABLE public.employment_types ALTER COLUMN organization_id SET NOT NULL;
    END IF;
END $$;

-- 6. ADD user reference columns and first_name, last_name
ALTER TABLE public.users ADD COLUMN first_name varchar(255);
ALTER TABLE public.users ADD COLUMN last_name varchar(255);
ALTER TABLE public.users ADD COLUMN department_id bigint;
ALTER TABLE public.users ADD COLUMN designation_id bigint;
ALTER TABLE public.users ADD COLUMN job_level_id bigint;
ALTER TABLE public.users ADD COLUMN employment_type_id bigint;
ALTER TABLE public.users ADD COLUMN reporting_manager_id bigint;

-- 7. Backfill user references if existing data requires it
-- (e.g. split full_name into first_name and last_name)
UPDATE public.users SET
    first_name = split_part(full_name, ' ', 1),
    last_name = CASE 
        WHEN position(' ' in full_name) > 0 THEN substring(full_name from position(' ' in full_name) + 1)
        ELSE ''
    END
WHERE full_name IS NOT NULL AND first_name IS NULL;

-- 8. ADD user FKs
ALTER TABLE public.users ADD CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES public.departments(id);
ALTER TABLE public.users ADD CONSTRAINT fk_users_designation FOREIGN KEY (designation_id) REFERENCES public.designations(id);
ALTER TABLE public.users ADD CONSTRAINT fk_users_job_level FOREIGN KEY (job_level_id) REFERENCES public.job_levels(id);
ALTER TABLE public.users ADD CONSTRAINT fk_users_employment_type FOREIGN KEY (employment_type_id) REFERENCES public.employment_types(id);
ALTER TABLE public.users ADD CONSTRAINT fk_users_reporting_manager FOREIGN KEY (reporting_manager_id) REFERENCES public.users(id);

-- 9. ADD indexes
CREATE INDEX idx_designation_org ON public.designations(organization_id);
CREATE INDEX idx_job_level_org ON public.job_levels(organization_id);
CREATE INDEX idx_employment_type_org ON public.employment_types(organization_id);

CREATE INDEX idx_users_department ON public.users(department_id);
CREATE INDEX idx_users_designation ON public.users(designation_id);
CREATE INDEX idx_users_job_level ON public.users(job_level_id);
CREATE INDEX idx_users_employment_type ON public.users(employment_type_id);
CREATE INDEX idx_users_reporting_manager ON public.users(reporting_manager_id);

-- 10. ADD unique constraints
ALTER TABLE public.departments ADD CONSTRAINT uk_departments_org_name UNIQUE (organization_id, name);
ALTER TABLE public.designations ADD CONSTRAINT uk_designations_org_title UNIQUE (organization_id, designation);
ALTER TABLE public.job_levels ADD CONSTRAINT uk_job_levels_org_des_title UNIQUE (organization_id, designation_id, job_level);
ALTER TABLE public.employment_types ADD CONSTRAINT uk_employment_types_org_jl_title UNIQUE (organization_id, job_level_id, employment_type);
