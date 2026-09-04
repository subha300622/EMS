-- Flyway migration V49: Create Generic Approval Workflow Engine tables
CREATE TABLE approval_workflow_definitions (
    id BIGSERIAL PRIMARY KEY,
    workflow_type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    organization_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_def_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_approval_wf_def_type_org ON approval_workflow_definitions(workflow_type, organization_id, status);

CREATE TABLE approval_workflow_steps (
    id BIGSERIAL PRIMARY KEY,
    workflow_definition_id BIGINT NOT NULL,
    step_order INT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    step_type VARCHAR(50) NOT NULL DEFAULT 'USER_APPROVAL',
    approver_type VARCHAR(50) NOT NULL,
    approver_config VARCHAR(255),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    sla_hours INT DEFAULT 48,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_step_def FOREIGN KEY (workflow_definition_id) REFERENCES approval_workflow_definitions(id) ON DELETE CASCADE
);

CREATE INDEX idx_approval_wf_steps_def ON approval_workflow_steps(workflow_definition_id, step_order);

CREATE TABLE approval_workflow_instances (
    id BIGSERIAL PRIMARY KEY,
    workflow_instance_id VARCHAR(100) NOT NULL UNIQUE,
    workflow_definition_id BIGINT,
    workflow_type VARCHAR(100) NOT NULL,
    organization_id BIGINT NOT NULL,
    business_reference_type VARCHAR(100) NOT NULL,
    business_reference_id VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    current_step INT NOT NULL DEFAULT 1,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_workflow_inst_org FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE INDEX idx_approval_wf_inst_org_ref ON approval_workflow_instances(organization_id, business_reference_type, business_reference_id);

CREATE TABLE approval_tasks (
    id BIGSERIAL PRIMARY KEY,
    approval_task_id VARCHAR(100) NOT NULL UNIQUE,
    workflow_instance_id BIGINT NOT NULL,
    step_id BIGINT,
    step_order INT NOT NULL,
    workflow_type VARCHAR(100) NOT NULL,
    business_reference_type VARCHAR(100) NOT NULL,
    business_reference_id VARCHAR(100) NOT NULL,
    approver_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_approval_task_inst FOREIGN KEY (workflow_instance_id) REFERENCES approval_workflow_instances(id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_task_approver FOREIGN KEY (approver_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX idx_approval_tasks_approver_status ON approval_tasks(approver_id, status);

CREATE TABLE approval_actions (
    id BIGSERIAL PRIMARY KEY,
    approval_task_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_action_task FOREIGN KEY (approval_task_id) REFERENCES approval_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_approval_action_actor FOREIGN KEY (actor_id) REFERENCES employees(id) ON DELETE CASCADE
);
