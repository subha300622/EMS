-- ====================================================================
-- V104: Add version column to platform_maintenance_config for optimistic locking
-- ====================================================================

ALTER TABLE platform_maintenance_config
ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
