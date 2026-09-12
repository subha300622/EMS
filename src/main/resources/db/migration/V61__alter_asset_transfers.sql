-- Flyway Migration V61: Add missing columns and drop not-null constraint on asset_transfers table

ALTER TABLE asset_transfers ALTER COLUMN from_location_id DROP NOT NULL;
ALTER TABLE asset_transfers ALTER COLUMN to_location_id DROP NOT NULL;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS from_department_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS to_department_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS transfer_reason VARCHAR(500);
