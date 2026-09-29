-- Migration script V41: Create Team Management tables with organization isolation and constraints

CREATE TABLE IF NOT EXISTS public.teams (
    id BIGSERIAL PRIMARY KEY,
    team_name VARCHAR(255) NOT NULL,
    team_code VARCHAR(255) NOT NULL,
    description TEXT,
    department_id BIGINT REFERENCES public.departments(id) ON DELETE SET NULL,
    team_lead_employee_id BIGINT REFERENCES public.employees(id) ON DELETE SET NULL,
    organization_id BIGINT REFERENCES public.organizations(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT uk_teams_org_name UNIQUE (organization_id, team_name),
    CONSTRAINT uk_teams_org_code UNIQUE (organization_id, team_code)
);

CREATE INDEX IF NOT EXISTS idx_teams_org ON public.teams(organization_id);
CREATE INDEX IF NOT EXISTS idx_teams_dept ON public.teams(department_id);
CREATE INDEX IF NOT EXISTS idx_teams_lead ON public.teams(team_lead_employee_id);
CREATE INDEX IF NOT EXISTS idx_teams_status ON public.teams(status);
CREATE INDEX IF NOT EXISTS idx_teams_deleted ON public.teams(deleted);

CREATE TABLE IF NOT EXISTS public.team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES public.teams(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES public.employees(id) ON DELETE CASCADE,
    joined_at DATE NOT NULL,
    left_at DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    is_team_lead BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_team_members_team ON public.team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_emp ON public.team_members(employee_id);
CREATE INDEX IF NOT EXISTS idx_team_members_status ON public.team_members(status);

CREATE TABLE IF NOT EXISTS public.team_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    performed_by BIGINT,
    performed_by_name VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

CREATE INDEX IF NOT EXISTS idx_team_audit_logs_team ON public.team_audit_logs(team_id);
