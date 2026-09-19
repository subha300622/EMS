CREATE TABLE department_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    department_id BIGINT NOT NULL,
    field VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    changed_by_user_id BIGINT,
    changed_by_user_name VARCHAR(255),
    changed_by_user_role VARCHAR(255),
    comment VARCHAR(255),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
