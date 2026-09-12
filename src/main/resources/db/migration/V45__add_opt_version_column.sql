-- Migration V45: Ensure opt_version column exists for optimistic locking on onboardings

ALTER TABLE onboardings ADD COLUMN IF NOT EXISTS opt_version BIGINT DEFAULT 0;
