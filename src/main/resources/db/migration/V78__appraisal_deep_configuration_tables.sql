-- Flyway Migration V78: Deep Appraisal Configuration, Versioning & Validation Tables

-- 1. Configuration Versions (Immutable Snapshots)
CREATE TABLE IF NOT EXISTS public.appraisal_configuration_versions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    snapshot_json TEXT NOT NULL,
    description VARCHAR(255),
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_cfg_ver_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_cfg_ver_creator FOREIGN KEY (created_by) REFERENCES public.employees(id) ON DELETE SET NULL,
    CONSTRAINT uq_appraisal_cfg_ver_org_num UNIQUE (organization_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_appraisal_cfg_ver_org ON public.appraisal_configuration_versions(organization_id, version_number DESC);

-- 2. Rating Scales
CREATE TABLE IF NOT EXISTS public.appraisal_rating_scales (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    scale_type VARCHAR(50) NOT NULL DEFAULT 'NUMERIC',
    min_rating DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    max_rating DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    step_value DOUBLE PRECISION DEFAULT 0.1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_scale_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appraisal_scale_org ON public.appraisal_rating_scales(organization_id, active);

-- 3. Rating Scale Levels
CREATE TABLE IF NOT EXISTS public.appraisal_rating_scale_levels (
    id BIGSERIAL PRIMARY KEY,
    rating_scale_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    min_score DOUBLE PRECISION NOT NULL,
    max_score DOUBLE PRECISION NOT NULL,
    description TEXT,
    level_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_scale_lvl_scale FOREIGN KEY (rating_scale_id) REFERENCES public.appraisal_rating_scales(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_scale_lvl_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appraisal_scale_lvl_scale ON public.appraisal_rating_scale_levels(rating_scale_id, level_order);

-- 4. Appraisal Criteria & Weights
CREATE TABLE IF NOT EXISTS public.appraisal_criteria (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_criteria_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT uq_appraisal_criteria_org_name UNIQUE (organization_id, name)
);

CREATE INDEX IF NOT EXISTS idx_appraisal_criteria_org ON public.appraisal_criteria(organization_id, active);

-- 5. Performance Categories
CREATE TABLE IF NOT EXISTS public.appraisal_performance_categories (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    min_rating DOUBLE PRECISION NOT NULL,
    max_rating DOUBLE PRECISION NOT NULL,
    description TEXT,
    color_code VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_perf_cat_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT uq_appraisal_perf_cat_org_name UNIQUE (organization_id, name)
);

CREATE INDEX IF NOT EXISTS idx_appraisal_perf_cat_org ON public.appraisal_performance_categories(organization_id, active);

-- 6. Increment Policies & Rules
CREATE TABLE IF NOT EXISTS public.appraisal_increment_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL DEFAULT 'Standard Increment Policy',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_inc_pol_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.appraisal_increment_rules (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    performance_category_id BIGINT,
    performance_category_name VARCHAR(100),
    min_rating DOUBLE PRECISION NOT NULL,
    max_rating DOUBLE PRECISION NOT NULL,
    eligible BOOLEAN NOT NULL DEFAULT TRUE,
    increment_percentage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    bonus_percentage DOUBLE PRECISION DEFAULT 0.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_inc_rule_pol FOREIGN KEY (policy_id) REFERENCES public.appraisal_increment_policies(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_inc_rule_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_inc_rule_cat FOREIGN KEY (performance_category_id) REFERENCES public.appraisal_performance_categories(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_appraisal_inc_rule_pol ON public.appraisal_increment_rules(policy_id, active);

-- 7. Bind Appraisal Cycles to Configuration Version Snapshot
ALTER TABLE public.appraisal_cycles ADD COLUMN IF NOT EXISTS configuration_version_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_appraisal_cycles_config_ver'
    ) THEN
        ALTER TABLE public.appraisal_cycles ADD CONSTRAINT fk_appraisal_cycles_config_ver FOREIGN KEY (configuration_version_id) REFERENCES public.appraisal_configuration_versions(id) ON DELETE SET NULL;
    END IF;
END $$;
