-- Migration V67: Create employee_salary_assignments table

CREATE TABLE IF NOT EXISTS employee_salary_assignments (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    salary_structure_id BIGINT NOT NULL REFERENCES salary_structures(id),
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_emp_sal_assign_org_emp ON employee_salary_assignments(organization_id, employee_id);
CREATE INDEX IF NOT EXISTS idx_emp_sal_assign_org_emp_eff ON employee_salary_assignments(organization_id, employee_id, effective_from);
CREATE INDEX IF NOT EXISTS idx_emp_sal_assign_org_status ON employee_salary_assignments(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_emp_sal_assign_struct ON employee_salary_assignments(salary_structure_id);
