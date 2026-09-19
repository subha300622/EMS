-- V52: Create Holidays Table for Organization-Wide Holiday Maintenance Module

CREATE TABLE IF NOT EXISTS public.holidays (
    id BIGSERIAL PRIMARY KEY,
    holiday_id VARCHAR(50) NOT NULL,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    holiday_date DATE NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT uk_holidays_org_date UNIQUE (organization_id, holiday_date),
    CONSTRAINT uk_holidays_org_holiday_id UNIQUE (organization_id, holiday_id)
);

CREATE INDEX IF NOT EXISTS idx_holidays_org_status_date ON public.holidays (organization_id, status, holiday_date);
