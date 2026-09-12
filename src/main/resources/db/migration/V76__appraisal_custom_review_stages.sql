-- Flyway Migration V76: Appraisal Custom Review Stages

CREATE TABLE IF NOT EXISTS public.appraisal_review_stages (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    configuration_id BIGINT,
    stage_order INT NOT NULL,
    stage_name VARCHAR(255) NOT NULL,
    required_permission VARCHAR(100) DEFAULT 'APPRAISAL_REVIEW',
    required BOOLEAN NOT NULL DEFAULT TRUE,
    weightage DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appraisal_review_stages_org FOREIGN KEY (organization_id) REFERENCES public.organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_appraisal_review_stages_config FOREIGN KEY (configuration_id) REFERENCES public.appraisal_configurations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_appraisal_review_stages_org_order ON public.appraisal_review_stages(organization_id, stage_order);
