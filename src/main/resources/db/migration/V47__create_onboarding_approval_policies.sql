CREATE TABLE IF NOT EXISTS onboarding_approval_policies (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    policy_id VARCHAR(50) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    next_status VARCHAR(50) NOT NULL,
    approver_type VARCHAR(50) NOT NULL,
    approver_role_id BIGINT,
    organization_configurable BOOLEAN DEFAULT TRUE,
    active BOOLEAN DEFAULT TRUE,
    conditions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_onb_pol_org ON onboarding_approval_policies(organization_id);
CREATE INDEX IF NOT EXISTS idx_onb_pol_policy_id ON onboarding_approval_policies(policy_id);
