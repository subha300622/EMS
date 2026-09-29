-- ====================================================================
-- V103: Create Platform Maintenance Mode Configuration and Permissions
-- Global Platform Admin Feature for Scheduling and Managing Platform Maintenance
-- ====================================================================

CREATE TABLE IF NOT EXISTS platform_maintenance_config (
    id BIGINT PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    allow_admin_access BOOLEAN NOT NULL DEFAULT TRUE,
    message VARCHAR(500),
    start_at TIMESTAMP WITH TIME ZONE,
    end_at TIMESTAMP WITH TIME ZONE,
    logout_active_sessions BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Seed singleton initial configuration row
INSERT INTO platform_maintenance_config (
    id, enabled, allow_admin_access, message, start_at, end_at, logout_active_sessions, updated_at, updated_by
) VALUES (
    1, FALSE, TRUE, 'The EMS platform is currently under maintenance.', NULL, NULL, FALSE, CURRENT_TIMESTAMP, 'SYSTEM'
) ON CONFLICT (id) DO NOTHING;

-- Seed platform maintenance permissions
INSERT INTO permissions (name, description) VALUES
('platform.maintenance.view', 'Permission to view platform maintenance configuration'),
('platform.maintenance.manage', 'Permission to manage platform maintenance configuration'),
('PLATFORM_MAINTENANCE_VIEW', 'Permission to view platform maintenance configuration'),
('PLATFORM_MAINTENANCE_MANAGE', 'Permission to manage platform maintenance configuration')
ON CONFLICT (name) DO NOTHING;

-- Map permissions to PLATFORM permission group if it exists
DO $$
DECLARE
    platform_group_id BIGINT;
    perm_id BIGINT;
BEGIN
    SELECT id INTO platform_group_id FROM permission_groups WHERE code = 'PLATFORM';
    IF platform_group_id IS NOT NULL THEN
        FOR perm_id IN
            SELECT id FROM permissions WHERE name IN (
                'platform.maintenance.view',
                'platform.maintenance.manage',
                'PLATFORM_MAINTENANCE_VIEW',
                'PLATFORM_MAINTENANCE_MANAGE'
            )
        LOOP
            INSERT INTO permission_group_mappings (group_id, permission_id)
            VALUES (platform_group_id, perm_id)
            ON CONFLICT DO NOTHING;
        END LOOP;
    END IF;
END $$;
