-- Flyway Migration V58: Enterprise Asset Management Module Expansion

-- Drop legacy skeleton tables if present
DROP TABLE IF EXISTS asset_action_requests CASCADE;
DROP TABLE IF EXISTS asset_histories CASCADE;
DROP TABLE IF EXISTS asset_history CASCADE;
DROP TABLE IF EXISTS asset_maintenances CASCADE;
DROP TABLE IF EXISTS asset_assignments CASCADE;
DROP TABLE IF EXISTS assets CASCADE;
DROP TABLE IF EXISTS asset_locations CASCADE;
DROP TABLE IF EXISTS asset_categories CASCADE;

-- 1. Asset Categories Table
CREATE TABLE asset_categories (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_cat_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT uq_asset_cat_code UNIQUE (organization_id, category_code),
    CONSTRAINT uq_asset_cat_name UNIQUE (organization_id, category_name)
);

CREATE INDEX idx_asset_cat_org ON asset_categories(organization_id, is_active);

-- 2. Asset Locations Table
CREATE TABLE asset_locations (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    location_code VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_loc_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_loc_parent FOREIGN KEY (parent_id) REFERENCES asset_locations(id) ON DELETE SET NULL,
    CONSTRAINT uq_asset_loc_code UNIQUE (organization_id, location_code),
    CONSTRAINT uq_asset_loc_name UNIQUE (organization_id, location_name)
);

CREATE INDEX idx_asset_loc_org ON asset_locations(organization_id, is_active);

-- 3. Core Assets Table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_code VARCHAR(50) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    serial_number VARCHAR(100),
    brand VARCHAR(100),
    model VARCHAR(100),
    purchase_date DATE NOT NULL,
    purchase_cost NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    current_value NUMERIC(15,2) DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    condition VARCHAR(50) NOT NULL DEFAULT 'GOOD',
    warranty_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    warranty_expiry_date DATE,
    vendor VARCHAR(255),
    description VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assets_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_assets_cat FOREIGN KEY (category_id) REFERENCES asset_categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_assets_loc FOREIGN KEY (location_id) REFERENCES asset_locations(id) ON DELETE RESTRICT,
    CONSTRAINT uq_assets_code UNIQUE (organization_id, asset_code)
);

CREATE INDEX idx_assets_org_status ON assets(organization_id, status, is_deleted);
CREATE INDEX idx_assets_cat_loc ON assets(category_id, location_id);

-- 4. Asset Assignments Table
CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    assigned_date DATE NOT NULL,
    expected_return_date DATE,
    returned_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    remarks VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_assign_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_assign_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE RESTRICT,
    CONSTRAINT fk_asset_assign_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT,
    CONSTRAINT fk_asset_assign_loc FOREIGN KEY (location_id) REFERENCES asset_locations(id) ON DELETE RESTRICT
);

CREATE INDEX idx_asset_assign_asset_status ON asset_assignments(asset_id, status);
CREATE INDEX idx_asset_assign_emp ON asset_assignments(employee_id, status);

-- Partial unique index to enforce maximum 1 active assignment per asset
CREATE UNIQUE INDEX uq_active_asset_assignment ON asset_assignments(asset_id) WHERE status = 'ACTIVE';

-- 5. Asset Maintenances Table
CREATE TABLE asset_maintenances (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_date DATE NOT NULL,
    start_date DATE,
    completed_date DATE,
    estimated_cost NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    actual_cost NUMERIC(15,2),
    vendor VARCHAR(255),
    technician VARCHAR(255),
    result VARCHAR(255),
    remarks VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_maint_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_maint_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE RESTRICT
);

CREATE INDEX idx_asset_maint_asset_status ON asset_maintenances(asset_id, status);

-- 6. Asset Append-Only Audit History Table
CREATE TABLE asset_histories (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    from_employee_id BIGINT,
    to_employee_id BIGINT,
    from_location_id BIGINT,
    to_location_id BIGINT,
    performed_by VARCHAR(255) NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reference_id VARCHAR(255),
    remarks VARCHAR(1000),
    CONSTRAINT fk_asset_hist_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_hist_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE INDEX idx_asset_hist_asset ON asset_histories(asset_id, performed_at);

-- 7. Asset Approval Action Requests Table
CREATE TABLE asset_action_requests (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    request_type VARCHAR(50) NOT NULL,
    approval_instance_id BIGINT UNIQUE,
    requested_by BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    payload_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_req_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_req_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE RESTRICT
);

CREATE INDEX idx_asset_req_asset_status ON asset_action_requests(asset_id, status);
