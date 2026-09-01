-- Flyway Migration V59: Seed Asset Management Permissions Master Records

INSERT INTO permissions (name, description) VALUES
('ASSET_VIEW', 'Permission to view assets'),
('ASSET_CREATE', 'Permission to create assets'),
('ASSET_UPDATE', 'Permission to update assets'),
('ASSET_DELETE', 'Permission to delete assets'),
('ASSET_CATEGORY_VIEW', 'Permission to view asset categories'),
('ASSET_CATEGORY_CREATE', 'Permission to create asset categories'),
('ASSET_CATEGORY_UPDATE', 'Permission to update asset categories'),
('ASSET_CATEGORY_DELETE', 'Permission to delete asset categories'),
('ASSET_LOCATION_VIEW', 'Permission to view asset locations'),
('ASSET_LOCATION_CREATE', 'Permission to create asset locations'),
('ASSET_LOCATION_UPDATE', 'Permission to update asset locations'),
('ASSET_LOCATION_DELETE', 'Permission to delete asset locations'),
('ASSET_ASSIGN', 'Permission to assign assets'),
('ASSET_TRANSFER', 'Permission to transfer assets'),
('ASSET_RETURN', 'Permission to return assets'),
('ASSET_HISTORY_VIEW', 'Permission to view asset audit history'),
('ASSET_MAINTENANCE_VIEW', 'Permission to view asset maintenance records'),
('ASSET_MAINTENANCE_CREATE', 'Permission to create asset maintenance schedules'),
('ASSET_MAINTENANCE_UPDATE', 'Permission to update asset maintenance records'),
('ASSET_MAINTENANCE_START', 'Permission to start asset maintenance'),
('ASSET_MAINTENANCE_COMPLETE', 'Permission to complete asset maintenance'),
('ASSET_MAINTENANCE_CANCEL', 'Permission to cancel asset maintenance'),
('ASSET_RETIRE', 'Permission to retire assets'),
('ASSET_DISPOSE', 'Permission to dispose assets')
ON CONFLICT (name) DO NOTHING;
