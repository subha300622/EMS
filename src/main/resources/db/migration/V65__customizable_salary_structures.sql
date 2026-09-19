-- Flyway Migration V65: Customizable Multi-Tenant Salary Structures Master & Versioning

-- 1. Seed missing permissions
INSERT INTO permissions (name, description) VALUES
('SALARY_STRUCTURE_VALIDATE', 'Permission to validate salary structures'),
('SALARY_STRUCTURE_DEACTIVATE', 'Permission to deactivate salary structures')
ON CONFLICT (name) DO NOTHING;

-- 2. Add master structure columns
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS name VARCHAR(150);
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS code VARCHAR(100);
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'INR';
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS pay_frequency VARCHAR(50) DEFAULT 'MONTHLY';
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS effective_from DATE;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS effective_to DATE;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'DRAFT';
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS created_by VARCHAR(150);
ALTER TABLE salary_structures ADD COLUMN IF NOT EXISTS updated_by VARCHAR(150);

-- 3. Make legacy employee-specific columns nullable to support master templates
ALTER TABLE salary_structures ALTER COLUMN employee_id DROP NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN basic_salary DROP NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN hra DROP NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN allowances DROP NOT NULL;

-- 4. Migrate existing records safely
DO $$
DECLARE
    default_org_id BIGINT;
BEGIN
    SELECT id INTO default_org_id FROM organizations ORDER BY id ASC LIMIT 1;
    IF default_org_id IS NULL THEN
        default_org_id := 1;
    END IF;

    UPDATE salary_structures
    SET organization_id = default_org_id
    WHERE organization_id IS NULL;
END $$;

UPDATE salary_structures
SET name = COALESCE(name, 'Default Structure ' || id),
    code = COALESCE(code, 'STRUCTURE_' || id),
    currency = COALESCE(currency, 'INR'),
    pay_frequency = COALESCE(pay_frequency, 'MONTHLY'),
    status = COALESCE(status, 'ACTIVE'),
    version = COALESCE(version, 1),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP)
WHERE name IS NULL OR code IS NULL;

-- 5. Set NOT NULL on core master fields
ALTER TABLE salary_structures ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN name SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN code SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN currency SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN pay_frequency SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN status SET NOT NULL;
ALTER TABLE salary_structures ALTER COLUMN version SET NOT NULL;

-- 6. Drop legacy unique constraint on employee_id if present
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'salary_structures_employee_id_key'
    ) THEN
        ALTER TABLE salary_structures DROP CONSTRAINT salary_structures_employee_id_key;
    END IF;
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_salary_structures_employee'
    ) THEN
        ALTER TABLE salary_structures DROP CONSTRAINT uk_salary_structures_employee;
    END IF;
END $$;

-- 7. Add unique constraint on (organization_id, code, version)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_salary_structures_org_code_version'
    ) THEN
        ALTER TABLE salary_structures ADD CONSTRAINT uk_salary_structures_org_code_version UNIQUE (organization_id, code, version);
    END IF;
END $$;

-- 8. Add indices for fast lookup
CREATE INDEX IF NOT EXISTS idx_salary_structures_org_status ON salary_structures (organization_id, status);
CREATE INDEX IF NOT EXISTS idx_salary_structures_org_code ON salary_structures (organization_id, code);
CREATE INDEX IF NOT EXISTS idx_salary_structures_org_effective ON salary_structures (organization_id, effective_from);
