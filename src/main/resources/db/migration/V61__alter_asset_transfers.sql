-- Flyway Migration V61: Add missing columns and drop not-null constraint on asset_transfers table

ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS from_location_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS to_location_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS from_department_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS to_department_id BIGINT;
ALTER TABLE asset_transfers ADD COLUMN IF NOT EXISTS transfer_reason VARCHAR(500);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'asset_transfers' AND column_name = 'from_location_id' AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE asset_transfers ALTER COLUMN from_location_id DROP NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'asset_transfers' AND column_name = 'to_location_id' AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE asset_transfers ALTER COLUMN to_location_id DROP NOT NULL;
    END IF;
END $$;

