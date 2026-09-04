-- Migration V68: Create employee_salary_component_values table

CREATE TABLE IF NOT EXISTS employee_salary_component_values (
    id BIGSERIAL PRIMARY KEY,
    salary_assignment_id BIGINT NOT NULL REFERENCES employee_salary_assignments(id) ON DELETE CASCADE,
    salary_component_id BIGINT NOT NULL REFERENCES salary_components(id),
    amount NUMERIC(12, 2),
    percentage NUMERIC(5, 2),
    override_type VARCHAR(30) NOT NULL DEFAULT 'FIXED_AMOUNT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_emp_sal_comp_val UNIQUE (salary_assignment_id, salary_component_id)
);

CREATE INDEX IF NOT EXISTS idx_emp_sal_comp_val_assign ON employee_salary_component_values(salary_assignment_id);
CREATE INDEX IF NOT EXISTS idx_emp_sal_comp_val_comp ON employee_salary_component_values(salary_component_id);
