-- ==============================================================================
-- Flyway Migration V90: Create Organization Compensation Feature Configurations
-- ==============================================================================

-- 1. Organization Compensation Configurations Table
CREATE TABLE IF NOT EXISTS organization_compensation_configs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    overtime_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    incentive_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    bonus_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_org_comp_cfg_org ON organization_compensation_configs(organization_id);

-- 2. Seed Permissions
INSERT INTO permissions (name, description)
VALUES
    ('COMPENSATION_CONFIG_VIEW', 'Permission to view organization variable compensation feature configurations'),
    ('COMPENSATION_CONFIG_MANAGE', 'Permission to update organization variable compensation feature configurations')
ON CONFLICT (name) DO NOTHING;

-- Map permissions to standard roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name IN ('COMPENSATION_CONFIG_VIEW', 'COMPENSATION_CONFIG_MANAGE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'HR'
  AND p.name IN ('COMPENSATION_CONFIG_VIEW', 'COMPENSATION_CONFIG_MANAGE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN ('COMPENSATION_CONFIG_VIEW')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
  AND p.name IN ('COMPENSATION_CONFIG_VIEW')
ON CONFLICT DO NOTHING;
