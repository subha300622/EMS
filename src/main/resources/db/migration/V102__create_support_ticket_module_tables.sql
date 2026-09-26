-- V102: Support Ticket Lifecycle, SLA, Escalation, and Work Log Tables

-- 1. Alter my_support_categories to add organization_id
ALTER TABLE my_support_categories ADD COLUMN IF NOT EXISTS organization_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_my_support_categories_org') THEN
        ALTER TABLE my_support_categories ADD CONSTRAINT fk_my_support_categories_org
            FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 2. Alter my_support_tickets to add assignment, hours, due date, status fields
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS assigned_to_id BIGINT;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS estimated_hours DOUBLE PRECISION;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS actual_hours DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS sla_hours INTEGER;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS is_overdue BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS is_escalated BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS escalation_level INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS source VARCHAR(255) DEFAULT 'WEB';
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(1000);
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS rejected_by VARCHAR(255);
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS duplicate_of_ticket_id BIGINT;
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS resolution VARCHAR(5000);
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS resolved_by VARCHAR(255);
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS closed_by VARCHAR(255);
ALTER TABLE my_support_tickets ADD COLUMN IF NOT EXISTS closure_comment VARCHAR(1000);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_my_support_tickets_assigned_to') THEN
        ALTER TABLE my_support_tickets ADD CONSTRAINT fk_my_support_tickets_assigned_to
            FOREIGN KEY (assigned_to_id) REFERENCES employees(id) ON DELETE SET NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_my_support_tickets_duplicate_of') THEN
        ALTER TABLE my_support_tickets ADD CONSTRAINT fk_my_support_tickets_duplicate_of
            FOREIGN KEY (duplicate_of_ticket_id) REFERENCES my_support_tickets(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 3. Alter my_support_comments to expand comment_text length to 5000
ALTER TABLE my_support_comments ALTER COLUMN comment_text TYPE VARCHAR(5000);

-- 4. Create support_ticket_status_history table
CREATE TABLE IF NOT EXISTS support_ticket_status_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES my_support_tickets(id) ON DELETE CASCADE,
    from_status VARCHAR(255),
    to_status VARCHAR(255) NOT NULL,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_st_status_hist_ticket ON support_ticket_status_history(ticket_id);

-- 5. Create support_escalation_history table with uniqueness on (ticket_id, level)
CREATE TABLE IF NOT EXISTS support_escalation_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES my_support_tickets(id) ON DELETE CASCADE,
    level INTEGER NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    triggered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    action VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'COMPLETED',
    CONSTRAINT uk_ticket_escalation_level UNIQUE (ticket_id, level)
);
CREATE INDEX IF NOT EXISTS idx_st_esc_hist_ticket ON support_escalation_history(ticket_id);

-- 6. Create support_work_logs table
CREATE TABLE IF NOT EXISTS support_work_logs (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES my_support_tickets(id) ON DELETE CASCADE,
    engineer_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,
    actual_hours DOUBLE PRECISION NOT NULL,
    description VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_st_work_logs_ticket ON support_work_logs(ticket_id);
CREATE INDEX IF NOT EXISTS idx_st_work_logs_engineer ON support_work_logs(engineer_id);

-- 7. Create support_sla_org_configs table
CREATE TABLE IF NOT EXISTS support_sla_org_configs (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 8. Create support_sla_org_rules table
CREATE TABLE IF NOT EXISTS support_sla_org_rules (
    id BIGSERIAL PRIMARY KEY,
    sla_config_id BIGINT NOT NULL REFERENCES support_sla_org_configs(id) ON DELETE CASCADE,
    priority VARCHAR(50) NOT NULL,
    sla_hours INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_st_sla_rules_config ON support_sla_org_rules(sla_config_id);

-- 9. Create support_escalation_rules table
CREATE TABLE IF NOT EXISTS support_escalation_rules (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    level INTEGER NOT NULL,
    trigger_after_minutes INTEGER NOT NULL,
    action VARCHAR(255) NOT NULL,
    CONSTRAINT uk_org_escalation_level UNIQUE (organization_id, level)
);
CREATE INDEX IF NOT EXISTS idx_st_esc_rules_org ON support_escalation_rules(organization_id);
