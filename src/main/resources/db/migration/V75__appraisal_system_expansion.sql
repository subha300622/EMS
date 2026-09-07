-- Flyway Migration V75: Appraisal System Expansion & Central Approval Integration

-- 1. Create appraisal_configurations table
CREATE TABLE IF NOT EXISTS public.appraisal_configurations (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    initiation_mode VARCHAR(50) NOT NULL DEFAULT 'HR_AND_EMPLOYEE',
    employee_request_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    approval_workflow_id BIGINT,
    review_workflow_id BIGINT,
    min_service_months INT DEFAULT 6,
    min_gap_months INT DEFAULT 6,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_config_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_config_wf FOREIGN KEY (approval_workflow_id) REFERENCES public.approval_workflow_definitions(id) ON DELETE SET NULL,
    CONSTRAINT fk_appraisal_config_review_wf FOREIGN KEY (review_workflow_id) REFERENCES public.approval_workflow_definitions(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_appraisal_config_org ON public.appraisal_configurations(organization_id);

-- 2. Create appraisal_request_reasons table
CREATE TABLE IF NOT EXISTS public.appraisal_request_reasons (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_reason_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT uq_appraisal_reason_org_code UNIQUE (organization_id, code)
);

CREATE INDEX IF NOT EXISTS idx_appraisal_reasons_org ON public.appraisal_request_reasons(organization_id, active);

-- 3. Create appraisal_requests table
CREATE TABLE IF NOT EXISTS public.appraisal_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    reason_id BIGINT NOT NULL,
    justification TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    approval_instance_id VARCHAR(100),
    appraisal_id BIGINT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    withdrawn_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_req_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_req_emp FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_req_reason FOREIGN KEY (reason_id) REFERENCES public.appraisal_request_reasons(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_appraisal_req_org_emp ON public.appraisal_requests(organization_id, employee_id, status);

-- 4. Alter appraisal_cycles table for tenant scoping and metadata
ALTER TABLE public.appraisal_cycles ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.appraisal_cycles ADD COLUMN IF NOT EXISTS type VARCHAR(50) DEFAULT 'ANNUAL';
ALTER TABLE public.appraisal_cycles ADD COLUMN IF NOT EXISTS eligible_criteria_json TEXT;
ALTER TABLE public.appraisal_cycles ADD COLUMN IF NOT EXISTS created_by BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_cycles_org'
    ) THEN
        ALTER TABLE public.appraisal_cycles ADD CONSTRAINT fk_appraisal_cycles_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_cycles_creator'
    ) THEN
        ALTER TABLE public.appraisal_cycles ADD CONSTRAINT fk_appraisal_cycles_creator FOREIGN KEY (created_by) REFERENCES public.employees(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 5. Alter appraisals table
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS request_id BIGINT;
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS current_stage_order INT DEFAULT 1;
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS performance_category VARCHAR(100);
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.appraisals ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.appraisals ALTER COLUMN cycle_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisals_org'
    ) THEN
        ALTER TABLE public.appraisals ADD CONSTRAINT fk_appraisals_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisals_request'
    ) THEN
        ALTER TABLE public.appraisals ADD CONSTRAINT fk_appraisals_request FOREIGN KEY (request_id) REFERENCES public.appraisal_requests(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_appraisals_org_emp ON public.appraisals(organization_id, employee_id, status);

-- 6. Create appraisal_assessments table
CREATE TABLE IF NOT EXISTS public.appraisal_assessments (
    id BIGSERIAL PRIMARY KEY,
    appraisal_id BIGINT NOT NULL UNIQUE,
    overall_rating DOUBLE PRECISION,
    strengths TEXT,
    achievements TEXT,
    development_areas TEXT,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_assessments_appraisal FOREIGN KEY (appraisal_id) REFERENCES public.appraisals(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appraisal_assessments_appraisal ON public.appraisal_assessments(appraisal_id);

-- 7. Create appraisal_reviews table
CREATE TABLE IF NOT EXISTS public.appraisal_reviews (
    id BIGSERIAL PRIMARY KEY,
    appraisal_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    stage_order INT NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    rating DOUBLE PRECISION,
    comments TEXT,
    recommendation VARCHAR(100),
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_reviews_appraisal FOREIGN KEY (appraisal_id) REFERENCES public.appraisals(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES public.employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appraisal_reviews_appraisal ON public.appraisal_reviews(appraisal_id, stage_order);

-- 8. Alter appraisal_history table
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS employee_id BIGINT;
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS change_type VARCHAR(100);
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS old_value TEXT;
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS new_value TEXT;
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS changed_by BIGINT;
ALTER TABLE public.appraisal_history ADD COLUMN IF NOT EXISTS comments TEXT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_history_org'
    ) THEN
        ALTER TABLE public.appraisal_history ADD CONSTRAINT fk_appraisal_history_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_history_emp'
    ) THEN
        ALTER TABLE public.appraisal_history ADD CONSTRAINT fk_appraisal_history_emp FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_history_changed_by'
    ) THEN
        ALTER TABLE public.appraisal_history ADD CONSTRAINT fk_appraisal_history_changed_by FOREIGN KEY (changed_by) REFERENCES public.employees(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 9. Seed Appraisal Permissions
INSERT INTO public.permissions (name, description, active) VALUES
('APPRAISAL_CONFIGURATION_VIEW', 'Permission to view appraisal configuration and request reasons', true),
('APPRAISAL_CONFIGURATION_MANAGE', 'Permission to configure appraisal policies and request reasons', true),
('APPRAISAL_REQUEST_CREATE', 'Permission to create an employee appraisal request', true),
('APPRAISAL_REQUEST_VIEW', 'Permission to view appraisal requests', true),
('APPRAISAL_REQUEST_SUBMIT', 'Permission to submit appraisal requests for approval', true),
('APPRAISAL_REQUEST_WITHDRAW', 'Permission to withdraw appraisal requests', true),
('APPRAISAL_REQUEST_APPROVE', 'Permission to approve employee appraisal requests', true),
('APPRAISAL_REQUEST_REJECT', 'Permission to reject employee appraisal requests', true),
('APPRAISAL_REQUEST_HOLD', 'Permission to put appraisal requests on hold', true),
('APPRAISAL_REQUEST_REQUEST_INFORMATION', 'Permission to request more information on appraisal requests', true),
('APPRAISAL_CYCLE_CREATE', 'Permission to create appraisal cycles', true),
('APPRAISAL_CYCLE_VIEW', 'Permission to view appraisal cycles', true),
('APPRAISAL_CYCLE_UPDATE', 'Permission to update appraisal cycles', true),
('APPRAISAL_CYCLE_ACTIVATE', 'Permission to activate appraisal cycles', true),
('APPRAISAL_CYCLE_CLOSE', 'Permission to close appraisal cycles', true),
('APPRAISAL_VIEW', 'Permission to view employee appraisals', true),
('APPRAISAL_ASSESS', 'Permission to submit employee self assessment', true),
('APPRAISAL_REVIEW', 'Permission to submit reviewer stage feedback and rating', true),
('APPRAISAL_COMPLETE', 'Permission to complete employee appraisal', true),
('APPRAISAL_PUBLISH', 'Permission to publish final appraisal results', true),
('APPRAISAL_HISTORY_VIEW', 'Permission to view appraisal history and audit logs', true)
ON CONFLICT (name) DO NOTHING;
