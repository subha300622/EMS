ALTER TABLE onboarding_templates ADD COLUMN description VARCHAR(255);
ALTER TABLE onboarding_templates ADD COLUMN department_id VARCHAR(255);
ALTER TABLE onboarding_templates ADD COLUMN designation VARCHAR(255);
ALTER TABLE onboarding_templates ADD COLUMN employment_type VARCHAR(255);
ALTER TABLE onboarding_templates ADD COLUMN experience_level VARCHAR(255);
ALTER TABLE onboarding_templates ADD COLUMN effective_to DATE;
ALTER TABLE onboarding_templates ADD COLUMN is_default BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE onboarding_templates ADD COLUMN status VARCHAR(255) DEFAULT 'ACTIVE';
ALTER TABLE onboarding_templates ADD COLUMN template_code VARCHAR(50);
ALTER TABLE onboarding_templates ADD COLUMN sections_json TEXT;
ALTER TABLE onboarding_templates ADD COLUMN documents_json TEXT;
ALTER TABLE onboarding_templates ADD COLUMN usage_count INTEGER DEFAULT 0 NOT NULL;

ALTER TABLE onboarding_templates ADD CONSTRAINT uk_onboarding_template_code UNIQUE (template_code);

CREATE UNIQUE INDEX uk_default_onboarding_template ON onboarding_templates (department_id, designation, employment_type) WHERE is_default = TRUE AND status = 'ACTIVE';
