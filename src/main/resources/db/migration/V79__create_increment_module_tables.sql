-- Flyway Migration V79: Increment Module Tables
-- Includes Enterprise Increment Policies, Policy Rating Bands, Cycles, Eligibility Evaluations, Recommendations, and Letters

-- 1. Enterprise Increment Policies
CREATE TABLE IF NOT EXISTS public.enterprise_increment_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    minimum_rating DOUBLE PRECISION NOT NULL DEFAULT 3.0,
    minimum_goal_achievement_percentage DOUBLE PRECISION DEFAULT 70.0,
    minimum_attendance_percentage DOUBLE PRECISION DEFAULT 90.0,
    minimum_service_months INT DEFAULT 12,
    maximum_increment_percentage DOUBLE PRECISION NOT NULL DEFAULT 20.0,
    minimum_increment_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    effective_date_rule VARCHAR(50) NOT NULL DEFAULT 'FIXED_DATE',
    effective_date DATE,
    budget_limit NUMERIC(15, 2) NOT NULL DEFAULT 0.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ent_inc_policy_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ent_inc_policy_org_active ON public.enterprise_increment_policies(organization_id, active);
CREATE INDEX IF NOT EXISTS idx_ent_inc_policy_org_ver ON public.enterprise_increment_policies(organization_id, version);

-- 2. Increment Policy Rating Bands (e.g. 4.5-5.0 -> 15%, 4.0-4.49 -> 12%)
CREATE TABLE IF NOT EXISTS public.increment_policy_bands (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    min_rating DOUBLE PRECISION NOT NULL,
    max_rating DOUBLE PRECISION NOT NULL,
    increment_percentage DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_increment_band_policy FOREIGN KEY (policy_id) REFERENCES public.enterprise_increment_policies(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_increment_band_policy ON public.increment_policy_bands(policy_id);

-- 3. Increment Cycles
CREATE TABLE IF NOT EXISTS public.increment_cycles (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    financial_year VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    effective_date DATE NOT NULL,
    policy_id BIGINT NOT NULL,
    budget_limit NUMERIC(15, 2) DEFAULT 0.0,
    allocated_budget NUMERIC(15, 2) DEFAULT 0.0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_increment_cycle_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_cycle_policy FOREIGN KEY (policy_id) REFERENCES public.enterprise_increment_policies(id)
);

CREATE INDEX IF NOT EXISTS idx_increment_cycle_org_status ON public.increment_cycles(organization_id, status);

-- 4. Increment Eligibility Evaluations (Audit & Snapshot of why employee is eligible/ineligible)
CREATE TABLE IF NOT EXISTS public.increment_eligibility_evaluations (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    cycle_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    appraisal_id BIGINT,
    eligible BOOLEAN NOT NULL,
    eligibility_status VARCHAR(50) NOT NULL,
    ineligible_reason TEXT,
    eligibility_snapshot TEXT,
    final_rating DOUBLE PRECISION,
    goal_achievement_percentage DOUBLE PRECISION,
    attendance_percentage DOUBLE PRECISION,
    service_months INT,
    disciplinary_clear BOOLEAN DEFAULT TRUE,
    budget_available BOOLEAN DEFAULT TRUE,
    evaluated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_increment_eval_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_eval_cycle FOREIGN KEY (cycle_id) REFERENCES public.increment_cycles(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_eval_emp FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_increment_eval_cycle_emp ON public.increment_eligibility_evaluations(cycle_id, employee_id);

-- 5. Increment Recommendations
CREATE TABLE IF NOT EXISTS public.increment_recommendations (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    cycle_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    appraisal_id BIGINT,
    current_salary NUMERIC(15, 2) NOT NULL,
    increment_percentage NUMERIC(5, 2) NOT NULL,
    increment_amount NUMERIC(15, 2) NOT NULL,
    recommended_salary NUMERIC(15, 2) NOT NULL,
    effective_date DATE NOT NULL,
    comments TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'RECOMMENDED',
    approval_request_id BIGINT,
    rejection_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_increment_rec_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_rec_cycle FOREIGN KEY (cycle_id) REFERENCES public.increment_cycles(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_rec_emp FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_increment_rec_cycle_emp ON public.increment_recommendations(cycle_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_increment_rec_org_status ON public.increment_recommendations(organization_id, status);

-- 6. Increment Letters (Generated post-implementation)
CREATE TABLE IF NOT EXISTS public.increment_letters (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    recommendation_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    letter_reference VARCHAR(100) NOT NULL,
    document_url VARCHAR(500),
    content TEXT,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_increment_letter_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_letter_rec FOREIGN KEY (recommendation_id) REFERENCES public.increment_recommendations(id) ON DELETE CASCADE,
    CONSTRAINT fk_increment_letter_emp FOREIGN KEY (employee_id) REFERENCES public.employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_increment_letter_rec ON public.increment_letters(recommendation_id);
CREATE INDEX IF NOT EXISTS idx_increment_letter_emp ON public.increment_letters(employee_id);
