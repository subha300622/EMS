-- =============================================================================
-- Flyway Migration: V98__create_enterprise_performance_management_module.sql
-- Enterprise Performance Management Orchestrator Domain
-- Multi-Tenant Composite FKs, Fail-Closed RLS, Append-Only Audit Trigger,
-- Historical Evidence Ledger, and Concurrency-Safe Idempotency
-- =============================================================================

-- Ensure employees has composite unique constraint for tenant-safe FK references
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_employees_org_id'
    ) THEN
        ALTER TABLE employees ADD CONSTRAINT uq_employees_org_id UNIQUE (organization_id, id);
    END IF;
END $$;

-- 1. Performance Review Cycles
CREATE TABLE IF NOT EXISTS performance_review_cycles (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    period_type VARCHAR(50) NOT NULL CHECK (period_type IN ('ANNUAL', 'BIANNUAL', 'QUARTERLY', 'MONTHLY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    self_review_deadline DATE,
    manager_review_deadline DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'EVALUATION', 'COMPLETED', 'ARCHIVED')),
    calculation_version INTEGER DEFAULT 1 CHECK (calculation_version > 0),
    formula_version VARCHAR(20) DEFAULT 'v1.0',
    -- Level-2 Component Weights (must sum to 100)
    kpi_weight NUMERIC(5,2) DEFAULT 50.00 CHECK (kpi_weight >= 0 AND kpi_weight <= 100),
    manager_weight NUMERIC(5,2) DEFAULT 30.00 CHECK (manager_weight >= 0 AND manager_weight <= 100),
    self_weight NUMERIC(5,2) DEFAULT 10.00 CHECK (self_weight >= 0 AND self_weight <= 100),
    attendance_weight NUMERIC(5,2) DEFAULT 10.00 CHECK (attendance_weight >= 0 AND attendance_weight <= 100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perf_cycle_org_code UNIQUE (organization_id, code),
    CONSTRAINT uq_perf_cycle_org_id UNIQUE (organization_id, id),
    CONSTRAINT chk_perf_cycle_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_perf_cycle_weights CHECK ((kpi_weight + manager_weight + self_weight + attendance_weight) = 100.00)
);

CREATE INDEX IF NOT EXISTS idx_perf_cycle_org ON performance_review_cycles(organization_id);
CREATE INDEX IF NOT EXISTS idx_perf_cycle_status ON performance_review_cycles(organization_id, status);

-- 2. Performance KPI Definitions
CREATE TABLE IF NOT EXISTS performance_kpi_definitions (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    cycle_id BIGINT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL CHECK (category IN ('TECHNICAL', 'OPERATIONAL', 'LEADERSHIP', 'CORE_VALUES', 'STRATEGIC')),
    measurement_type VARCHAR(50) NOT NULL CHECK (measurement_type IN ('HIGHER_IS_BETTER', 'LOWER_IS_BETTER', 'PERCENTAGE', 'BOOLEAN', 'RATING_BASED')),
    default_weight NUMERIC(5,2) DEFAULT 0 CHECK (default_weight >= 0 AND default_weight <= 100),
    min_threshold NUMERIC(10,2) DEFAULT 0 CHECK (min_threshold >= 0 AND min_threshold <= 100),
    max_threshold NUMERIC(10,2) DEFAULT 100 CHECK (max_threshold >= 0 AND max_threshold <= 100),
    target_value NUMERIC(10,2),
    unit VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE')),
    is_mandatory BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perf_kpi_org_id UNIQUE (organization_id, id),
    CONSTRAINT uq_perf_kpi_org_cycle_code UNIQUE (organization_id, cycle_id, code),
    CONSTRAINT chk_perf_kpi_thresholds CHECK (max_threshold >= min_threshold),
    CONSTRAINT fk_perf_kpi_cycle FOREIGN KEY (organization_id, cycle_id)
        REFERENCES performance_review_cycles(organization_id, id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_perf_kpi_org ON performance_kpi_definitions(organization_id);
CREATE INDEX IF NOT EXISTS idx_perf_kpi_cycle ON performance_kpi_definitions(cycle_id);

-- 3. Performance Review Records (The Orchestrator)
CREATE TABLE IF NOT EXISTS performance_review_records (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    cycle_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    reviewer_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN (
        'DRAFT', 'SELF_REVIEW_PENDING', 'SELF_REVIEW_SUBMITTED', 'MANAGER_REVIEW_PENDING',
        'MANAGER_REVIEW_SUBMITTED', 'CALCULATED', 'SUBMITTED', 'APPROVAL_PENDING',
        'APPROVED', 'REJECTED', 'PUBLISHED', 'LOCKED', 'CANCELLED'
    )),
    -- Self Review Inputs
    self_score NUMERIC(5,2) CHECK (self_score IS NULL OR (self_score >= 0 AND self_score <= 100)),
    self_feedback TEXT,
    self_submitted_at TIMESTAMP WITH TIME ZONE,
    -- Manager Review Inputs
    manager_score NUMERIC(5,2) CHECK (manager_score IS NULL OR (manager_score >= 0 AND manager_score <= 100)),
    manager_feedback TEXT,
    manager_submitted_at TIMESTAMP WITH TIME ZONE,
    -- Snapshotted Attendance Metrics
    attendance_score NUMERIC(5,2) DEFAULT 0 CHECK (attendance_score >= 0 AND attendance_score <= 100),
    leaves_taken INTEGER DEFAULT 0,
    attendance_percentage NUMERIC(5,2) DEFAULT 100.00,
    -- Level-1 and Level-2 Calculated Scores
    kpi_weighted_score NUMERIC(5,2) DEFAULT 0 CHECK (kpi_weighted_score >= 0 AND kpi_weighted_score <= 100),
    calculated_score NUMERIC(5,2) DEFAULT 0 CHECK (calculated_score >= 0 AND calculated_score <= 100),
    final_score NUMERIC(5,2) DEFAULT 0 CHECK (final_score >= 0 AND final_score <= 100),
    rating_band VARCHAR(50) CHECK (rating_band IS NULL OR rating_band IN ('OUTSTANDING', 'EXCEEDS_EXPECTATIONS', 'MEETS_EXPECTATIONS', 'NEEDS_IMPROVEMENT', 'UNSATISFACTORY')),
    -- Snapshot and Calculation Tracking
    calculation_version INTEGER DEFAULT 1 CHECK (calculation_version > 0),
    formula_version VARCHAR(20) DEFAULT 'v1.0',
    current_calculation_run_id BIGINT,
    snapshot_data JSONB,
    snapshot_version INTEGER DEFAULT 1 CHECK (snapshot_version > 0),
    snapshot_hash VARCHAR(64),
    snapshot_created_at TIMESTAMP WITH TIME ZONE,
    calculated_at TIMESTAMP WITH TIME ZONE,
    calculated_by_id BIGINT,
    -- Approval Integration
    submitted_at TIMESTAMP WITH TIME ZONE,
    submitted_by_id BIGINT,
    approval_workflow_id BIGINT,
    approved_at TIMESTAMP WITH TIME ZONE,
    approved_by_id BIGINT,
    rejection_reason TEXT,
    rejected_at TIMESTAMP WITH TIME ZONE,
    rejected_by_id BIGINT,
    reopened_at TIMESTAMP WITH TIME ZONE,
    reopened_by_id BIGINT,
    -- Publishing and Locking
    published_at TIMESTAMP WITH TIME ZONE,
    published_by_id BIGINT,
    locked_at TIMESTAMP WITH TIME ZONE,
    locked_by_id BIGINT,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perf_review_org_id UNIQUE (organization_id, id),
    CONSTRAINT uq_perf_review_cycle_emp UNIQUE (organization_id, cycle_id, employee_id),
    CONSTRAINT fk_perf_review_cycle FOREIGN KEY (organization_id, cycle_id)
        REFERENCES performance_review_cycles(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_emp FOREIGN KEY (organization_id, employee_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_reviewer FOREIGN KEY (organization_id, reviewer_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_calc_by FOREIGN KEY (organization_id, calculated_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_sub_by FOREIGN KEY (organization_id, submitted_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_appr_by FOREIGN KEY (organization_id, approved_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_rej_by FOREIGN KEY (organization_id, rejected_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_reopen_by FOREIGN KEY (organization_id, reopened_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_pub_by FOREIGN KEY (organization_id, published_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_review_lock_by FOREIGN KEY (organization_id, locked_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_perf_review_org_status ON performance_review_records(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_perf_review_employee ON performance_review_records(employee_id);
CREATE INDEX IF NOT EXISTS idx_perf_review_cycle ON performance_review_records(cycle_id);

-- 4. Performance Review KPI Scores
CREATE TABLE IF NOT EXISTS performance_review_kpi_scores (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    kpi_id BIGINT NOT NULL,
    weight NUMERIC(5,2) NOT NULL CHECK (weight >= 0 AND weight <= 100),
    target_value NUMERIC(10,2),
    actual_value NUMERIC(10,2),
    normalized_score NUMERIC(5,2) DEFAULT 0 CHECK (normalized_score >= 0 AND normalized_score <= 100),
    comments TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perf_kpi_score_org_review_kpi UNIQUE (organization_id, review_id, kpi_id),
    CONSTRAINT fk_perf_score_review FOREIGN KEY (organization_id, review_id)
        REFERENCES performance_review_records(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_score_kpi FOREIGN KEY (organization_id, kpi_id)
        REFERENCES performance_kpi_definitions(organization_id, id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_perf_kpi_scores_review ON performance_review_kpi_scores(review_id);

-- 5. Performance Calculation Runs (Historical Evidence Ledger)
CREATE TABLE IF NOT EXISTS performance_calculation_runs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    review_id BIGINT NOT NULL,
    calculation_version INTEGER NOT NULL CHECK (calculation_version > 0),
    formula_version VARCHAR(20) NOT NULL,
    input_snapshot_hash VARCHAR(64) NOT NULL,
    kpi_score NUMERIC(5,2) NOT NULL CHECK (kpi_score >= 0 AND kpi_score <= 100),
    manager_score NUMERIC(5,2) NOT NULL CHECK (manager_score >= 0 AND manager_score <= 100),
    self_score NUMERIC(5,2) NOT NULL CHECK (self_score >= 0 AND self_score <= 100),
    attendance_score NUMERIC(5,2) NOT NULL CHECK (attendance_score >= 0 AND attendance_score <= 100),
    final_score NUMERIC(5,2) NOT NULL CHECK (final_score >= 0 AND final_score <= 100),
    rating_band VARCHAR(50) NOT NULL CHECK (rating_band IN (
        'OUTSTANDING', 'EXCEEDS_EXPECTATIONS', 'MEETS_EXPECTATIONS', 'NEEDS_IMPROVEMENT', 'UNSATISFACTORY'
    )),
    calculated_by_id BIGINT,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_perf_calc_run_org_id UNIQUE (organization_id, id),
    CONSTRAINT uq_perf_calc_run_version UNIQUE (organization_id, review_id, calculation_version),
    CONSTRAINT fk_perf_calc_run_review FOREIGN KEY (organization_id, review_id)
        REFERENCES performance_review_records(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_calc_run_calc_by FOREIGN KEY (organization_id, calculated_by_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_perf_calc_runs_review ON performance_calculation_runs(review_id);

-- Add composite foreign key from performance_review_records to performance_calculation_runs
ALTER TABLE performance_review_records
    ADD CONSTRAINT fk_perf_review_current_calc
    FOREIGN KEY (organization_id, current_calculation_run_id)
    REFERENCES performance_calculation_runs(organization_id, id)
    ON DELETE RESTRICT;

-- 6. Performance Command Idempotency
CREATE TABLE IF NOT EXISTS performance_command_idempotency (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(128) NOT NULL,
    command_type VARCHAR(50) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    resource_id BIGINT NOT NULL,
    response_status INTEGER NOT NULL,
    response_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_perf_idempotency_org_key UNIQUE (organization_id, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_perf_idempotency_lookup 
    ON performance_command_idempotency(organization_id, idempotency_key, command_type);

-- 7. Performance Review Audits (Append-Only Immutable Ledger)
CREATE TABLE IF NOT EXISTS performance_review_audits (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    review_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT,
    actor_name VARCHAR(255),
    before_status VARCHAR(50),
    after_status VARCHAR(50),
    before_score NUMERIC(5,2),
    after_score NUMERIC(5,2),
    reason TEXT,
    snapshot_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perf_audit_review FOREIGN KEY (organization_id, review_id)
        REFERENCES performance_review_records(organization_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_perf_audit_actor FOREIGN KEY (organization_id, actor_id)
        REFERENCES employees(organization_id, id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_perf_audits_org ON performance_review_audits(organization_id);
CREATE INDEX IF NOT EXISTS idx_perf_audits_review ON performance_review_audits(review_id);

-- 8. Fail-Closed Row Level Security (RLS) on all 7 tables
ALTER TABLE performance_review_cycles ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_review_cycles FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_cycles_policy ON performance_review_cycles;
CREATE POLICY tenant_isolation_perf_cycles_policy ON performance_review_cycles FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_kpi_definitions ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_kpi_definitions FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_kpi_policy ON performance_kpi_definitions;
CREATE POLICY tenant_isolation_perf_kpi_policy ON performance_kpi_definitions FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_review_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_review_records FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_review_policy ON performance_review_records;
CREATE POLICY tenant_isolation_perf_review_policy ON performance_review_records FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_review_kpi_scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_review_kpi_scores FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_kpi_scores_policy ON performance_review_kpi_scores;
CREATE POLICY tenant_isolation_perf_kpi_scores_policy ON performance_review_kpi_scores FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_calculation_runs ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_calculation_runs FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_calc_runs_policy ON performance_calculation_runs;
CREATE POLICY tenant_isolation_perf_calc_runs_policy ON performance_calculation_runs FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_command_idempotency ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_command_idempotency FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_idempotency_policy ON performance_command_idempotency;
CREATE POLICY tenant_isolation_perf_idempotency_policy ON performance_command_idempotency FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

ALTER TABLE performance_review_audits ENABLE ROW LEVEL SECURITY;
ALTER TABLE performance_review_audits FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS tenant_isolation_perf_audits_policy ON performance_review_audits;
CREATE POLICY tenant_isolation_perf_audits_policy ON performance_review_audits FOR ALL
    USING (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true')
    WITH CHECK (organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint OR current_setting('app.is_platform_admin', true) = 'true');

-- 9. Database-Enforced Audit Immutability Trigger
CREATE OR REPLACE FUNCTION trg_prevent_perf_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit records are immutable and cannot be updated or deleted.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_perf_audits_immutable ON performance_review_audits;
CREATE TRIGGER trg_perf_audits_immutable
    BEFORE UPDATE OR DELETE ON performance_review_audits
    FOR EACH ROW
    EXECUTE FUNCTION trg_prevent_perf_audit_modification();
