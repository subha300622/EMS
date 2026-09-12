-- Migration V69: Make legacy type column in salary_components nullable or synchronized

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'salary_components' AND column_name = 'type'
    ) THEN
        ALTER TABLE salary_components ALTER COLUMN type DROP NOT NULL;
    END IF;
END $$;
