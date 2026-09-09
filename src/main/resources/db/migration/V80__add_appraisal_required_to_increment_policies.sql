-- Flyway Migration V80: Add appraisal_required to Enterprise Increment Policies

ALTER TABLE public.enterprise_increment_policies
ADD COLUMN IF NOT EXISTS appraisal_required BOOLEAN NOT NULL DEFAULT FALSE;
