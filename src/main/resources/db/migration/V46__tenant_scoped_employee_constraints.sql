-- Migration V46: Tenant-Scoped Unique Constraints for Employees Table
ALTER TABLE employees DROP CONSTRAINT IF EXISTS ukj9xgmd0ya5jmus09o0b8pqrpb;
ALTER TABLE employees DROP CONSTRAINT IF EXISTS ukovvvp79dq21byf7svnuekb6iw;

-- Re-add tenant-scoped unique constraints
ALTER TABLE employees ADD CONSTRAINT uk_employees_org_email UNIQUE (organization_id, email);
ALTER TABLE employees ADD CONSTRAINT uk_employees_org_emp_id UNIQUE (organization_id, employee_id);
