-- Flyway Migration V60: Add Asset Documents and Asset Transfers Tables

DROP TABLE IF EXISTS asset_documents CASCADE;
DROP TABLE IF EXISTS asset_transfers CASCADE;

-- 1. Asset Documents Table
CREATE TABLE asset_documents (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'DATABASE',
    storage_key VARCHAR(500),
    file_data BYTEA,
    uploaded_by VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_doc_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_doc_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE INDEX idx_asset_doc_asset ON asset_documents(asset_id);

-- 2. Asset Transfers Table
CREATE TABLE asset_transfers (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    from_employee_id BIGINT,
    to_employee_id BIGINT,
    from_department_id BIGINT,
    to_department_id BIGINT,
    transfer_reason VARCHAR(500),
    transferred_by VARCHAR(255) NOT NULL,
    transferred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asset_trf_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_asset_trf_asset FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE RESTRICT
);

CREATE INDEX idx_asset_trf_asset ON asset_transfers(asset_id);
