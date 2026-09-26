-- Flyway Migration V64: Customizable Multi-Tenant Salary Components Master Catalog

-- 1. Seed Salary Component Permissions
INSERT INTO permissions (name, description) VALUES
('SALARY_COMPONENT_CREATE', 'Permission to create salary components in organization catalog'),
('SALARY_COMPONENT_VIEW', 'Permission to view salary components in organization catalog'),
('SALARY_COMPONENT_UPDATE', 'Permission to update salary components in organization catalog'),
('SALARY_COMPONENT_DELETE', 'Permission to deactivate/delete salary components in organization catalog'),
('SALARY_STRUCTURE_CREATE', 'Permission to create salary structures'),
('SALARY_STRUCTURE_VIEW', 'Permission to view salary structures'),
('SALARY_STRUCTURE_UPDATE', 'Permission to update salary structures'),
('SALARY_STRUCTURE_DELETE', 'Permission to delete salary structures'),
('SALARY_STRUCTURE_ACTIVATE', 'Permission to activate salary structures'),
('EMPLOYEE_SALARY_ASSIGN', 'Permission to assign salary structure to employees'),
('EMPLOYEE_SALARY_VIEW', 'Permission to view employee salary details'),
('EMPLOYEE_SALARY_UPDATE', 'Permission to update employee salary overrides'),
('EMPLOYEE_SALARY_HISTORY_VIEW', 'Permission to view employee salary assignment history'),
('SALARY_CALCULATE', 'Permission to execute salary calculation engine')
ON CONFLICT (name) DO NOTHING;

-- 2. Add columns if not present
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS organization_id BIGINT;
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS code VARCHAR(100);
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS component_type VARCHAR(50);
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT true;
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE salary_components ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- 3. Migrate existing data safely
-- Map organization_id if null to first organization or default 1
DO $$
DECLARE
    default_org_id BIGINT;
BEGIN
    SELECT id INTO default_org_id FROM organizations ORDER BY id ASC LIMIT 1;
    IF default_org_id IS NULL THEN
        default_org_id := 1;
    END IF;

    UPDATE salary_components
    SET organization_id = default_org_id
    WHERE organization_id IS NULL;
END $$;

-- Migrate code from name if null
UPDATE salary_components
SET code = UPPER(REGEXP_REPLACE(name, '[^a-zA-Z0-9]+', '_', 'g'))
WHERE code IS NULL;

-- Migrate component_type from type if null
UPDATE salary_components
SET component_type = COALESCE(type, 'EARNING')
WHERE component_type IS NULL;

-- Migrate active flag if null
UPDATE salary_components
SET active = true
WHERE active IS NULL;

-- 4. Apply NOT NULL constraints and defaults
ALTER TABLE salary_components ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE salary_components ALTER COLUMN code SET NOT NULL;
ALTER TABLE salary_components ALTER COLUMN component_type SET NOT NULL;
ALTER TABLE salary_components ALTER COLUMN active SET NOT NULL;

-- 5. Drop old global unique constraint on name if exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uklan58rpgjrygnyr6aujdg9cdm'
    ) THEN
        ALTER TABLE salary_components DROP CONSTRAINT uklan58rpgjrygnyr6aujdg9cdm;
    END IF;

    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'salary_components_name_key'
    ) THEN
        ALTER TABLE salary_components DROP CONSTRAINT salary_components_name_key;
    END IF;
END $$;

-- 6. Add organization-scoped unique constraint on code
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_salary_components_org_code'
    ) THEN
        ALTER TABLE salary_components ADD CONSTRAINT uk_salary_components_org_code UNIQUE (organization_id, code);
    END IF;
END $$;

-- 7. Add indices for high-frequency queries
CREATE INDEX IF NOT EXISTS idx_salary_components_org_active ON salary_components (organization_id, active);
CREATE INDEX IF NOT EXISTS idx_salary_components_org_type ON salary_components (organization_id, component_type);
