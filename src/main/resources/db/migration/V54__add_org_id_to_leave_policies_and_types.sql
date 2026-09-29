-- V54: Add organization_id to leave_policies and leave_types for multitenancy schema validation

ALTER TABLE public.leave_policies ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE public.leave_types ADD COLUMN IF NOT EXISTS organization_id BIGINT;
