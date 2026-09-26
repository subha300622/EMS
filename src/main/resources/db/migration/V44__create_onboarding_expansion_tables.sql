-- Migration V44: Additional Onboarding Tables for Comments, Approvals, Audit Logs & Task Extensions

CREATE TABLE IF NOT EXISTS onboarding_comments (
    id BIGSERIAL PRIMARY KEY,
    onboarding_id BIGINT NOT NULL REFERENCES onboardings(id) ON DELETE CASCADE,
    comment TEXT NOT NULL,
    created_by_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS onboarding_approvals (
    id BIGSERIAL PRIMARY KEY,
    onboarding_id BIGINT NOT NULL REFERENCES onboardings(id) ON DELETE CASCADE,
    level INT NOT NULL DEFAULT 1,
    approver_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    approved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS onboarding_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    onboarding_id BIGINT NOT NULL REFERENCES onboardings(id) ON DELETE CASCADE,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    performed_by VARCHAR(100),
    performed_by_name VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

ALTER TABLE onboarding_tasks ADD COLUMN IF NOT EXISTS assigned_to_id BIGINT REFERENCES employees(id);
ALTER TABLE onboarding_tasks ADD COLUMN IF NOT EXISTS remarks TEXT;
ALTER TABLE onboarding_tasks ADD COLUMN IF NOT EXISTS phase_id BIGINT;
ALTER TABLE onboarding_tasks ADD COLUMN IF NOT EXISTS document_id BIGINT;
