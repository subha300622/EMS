-- Flyway migration V51: Seed default SCHEDULE_SWAP workflow definition and steps
INSERT INTO approval_workflow_definitions (workflow_type, name, version, organization_id, status, created_at, updated_at)
VALUES ('SCHEDULE_SWAP', 'Schedule Swap Default Workflow', 1, NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO approval_workflow_steps (workflow_definition_id, step_order, step_name, step_type, approver_type, approver_config, required, sla_hours)
SELECT id, 1, 'Target Employee Consent', 'USER_APPROVAL', 'TARGET_EMPLOYEE', NULL, TRUE, 24
FROM approval_workflow_definitions WHERE workflow_type = 'SCHEDULE_SWAP' AND version = 1 LIMIT 1;

INSERT INTO approval_workflow_steps (workflow_definition_id, step_order, step_name, step_type, approver_type, approver_config, required, sla_hours)
SELECT id, 2, 'Direct Manager Approval', 'USER_APPROVAL', 'DIRECT_MANAGER', NULL, TRUE, 48
FROM approval_workflow_definitions WHERE workflow_type = 'SCHEDULE_SWAP' AND version = 1 LIMIT 1;
