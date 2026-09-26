CREATE TABLE IF NOT EXISTS schedule_exceptions (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(100) NOT NULL,
    exception_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_request_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sched_exc_leave_req_type UNIQUE (leave_request_id, exception_type)
);

CREATE INDEX IF NOT EXISTS idx_sched_exc_emp_dates ON schedule_exceptions(employee_id, start_date, end_date, status);
CREATE INDEX IF NOT EXISTS idx_sched_exc_leave_req ON schedule_exceptions(leave_request_id);
