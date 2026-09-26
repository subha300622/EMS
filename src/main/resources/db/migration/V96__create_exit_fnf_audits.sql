-- =============================================================================
-- Flyway Migration: V96__create_exit_fnf_audits.sql
-- Financial audit trail and snapshot history for Exit F&F Settlements
-- =============================================================================

CREATE TABLE IF NOT EXISTS exit_fnf_audits (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    settlement_id BIGINT NOT NULL REFERENCES exit_fnf_settlements(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT REFERENCES employees(id),
    actor_name VARCHAR(255),
    before_amount NUMERIC(15, 2),
    after_amount NUMERIC(15, 2),
    reason TEXT,
    snapshot_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_exit_fnf_audits_settlement ON exit_fnf_audits(settlement_id);
CREATE INDEX IF NOT EXISTS idx_exit_fnf_audits_org ON exit_fnf_audits(organization_id);

ALTER TABLE exit_fnf_audits ENABLE ROW LEVEL SECURITY;
ALTER TABLE exit_fnf_audits FORCE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS tenant_isolation_exit_fnf_audits_policy ON exit_fnf_audits;
CREATE POLICY tenant_isolation_exit_fnf_audits_policy ON exit_fnf_audits FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');
