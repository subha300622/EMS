-- Flyway migration V50: Create schedule_swap_requests table
CREATE TABLE schedule_swap_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL,
    source_schedule_id BIGINT NOT NULL,
    source_employee_id BIGINT NOT NULL,
    target_schedule_id BIGINT NOT NULL,
    target_employee_id BIGINT NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    workflow_instance_id VARCHAR(100),
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_swap_req_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_req_src_sch FOREIGN KEY (source_schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_req_src_emp FOREIGN KEY (source_employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_req_tgt_sch FOREIGN KEY (target_schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_req_tgt_emp FOREIGN KEY (target_employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_swap_req_creator FOREIGN KEY (created_by) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX idx_swap_req_org_status ON schedule_swap_requests(organization_id, status);
CREATE INDEX idx_swap_req_schedules ON schedule_swap_requests(source_schedule_id, target_schedule_id);
