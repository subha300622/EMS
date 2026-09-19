package com.example.ems.employee.domain;

import com.example.ems.organization.entity.Organization;
import com.example.ems.auth.entity.Role;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Designation;
import com.example.ems.employee.entity.JobLevel;
import com.example.ems.employee.entity.EmploymentType;
import com.example.ems.auth.entity.User;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EmploymentAssignmentValidator {

    public void validateEmployeeAssignments(Employee employee, Department department, Employee reportingManager, EmployeeRepository employeeRepository) {
        if (employee == null) {
            throw new BadRequestException("Employee is required");
        }
        Organization organization = employee.getOrganization();
        if (organization == null) {
            throw new BadRequestException("Organization is required for active employee");
        }
        if (organization.getStatus() != null && !"ACTIVE".equalsIgnoreCase(organization.getStatus().name())) {
            throw new BadRequestException("Organization is not active");
        }

        if (department != null) {
            if (!"ACTIVE".equalsIgnoreCase(department.getStatus())) {
                throw new BadRequestException("Department is not active");
            }
            if (department.getOrganization() == null || !department.getOrganization().getId().equals(organization.getId())) {
                throw new BadRequestException("Department does not belong to the employee's organization");
            }
        }

        if (reportingManager != null) {
            validateManager(employee, reportingManager, employeeRepository);
        }
    }

    public void validateManagerChange(Employee employee, Employee newManager, EmployeeRepository employeeRepository) {
        if (employee == null) {
            throw new BadRequestException("Employee is required");
        }
        if (newManager == null) {
            throw new BadRequestException("New reporting manager is required");
        }
        validateManager(employee, newManager, employeeRepository);
    }

    public void validateDepartmentTransfer(Employee employee, Department fromDept, Department toDept) {
        if (employee == null) {
            throw new BadRequestException("Employee is required");
        }
        if (toDept == null) {
            throw new BadRequestException("Destination department is required");
        }
        Organization org = employee.getOrganization();
        if (org == null) {
            throw new BadRequestException("Employee does not belong to an organization");
        }
        if (toDept.getOrganization() == null || !toDept.getOrganization().getId().equals(org.getId())) {
            throw new BadRequestException("Destination department does not belong to the employee's organization");
        }
        if (!"ACTIVE".equalsIgnoreCase(toDept.getStatus())) {
            throw new BadRequestException("Destination department is not active");
        }
        if (fromDept != null) {
            if (fromDept.getOrganization() == null || !fromDept.getOrganization().getId().equals(org.getId())) {
                throw new BadRequestException("Source department does not belong to the employee's organization");
            }
        }
    }

    private void validateManager(Employee employee, Employee manager, EmployeeRepository employeeRepository) {
        Organization organization = employee.getOrganization();
        if (organization == null) {
            throw new BadRequestException("Employee does not belong to an organization");
        }
        if (employee.getId() != null && employee.getId().equals(manager.getId())) {
            throw new BadRequestException("Employee cannot report to themselves");
        }
        if (!"ACTIVE".equalsIgnoreCase(manager.getStatus())) {
            throw new BadRequestException("Reporting Manager is not active");
        }
        if (manager.getOrganization() == null || !manager.getOrganization().getId().equals(organization.getId())) {
            throw new BadRequestException("Reporting Manager does not belong to the same organization");
        }

        // Circular reporting chain detection
        if (employee.getId() != null) {
            Set<Long> visited = new HashSet<>();
            visited.add(employee.getId());

            Employee current = manager;
            while (current != null) {
                if (visited.contains(current.getId())) {
                    throw new BadRequestException("Circular reporting chain detected");
                }
                visited.add(current.getId());
                if (current.getManager() != null) {
                    current = current.getManager();
                } else if (employeeRepository != null && current.getId() != null) {
                    current = employeeRepository.findById(current.getId()).map(Employee::getManager).orElse(null);
                } else {
                    break;
                }
            }
        }
    }

    public void validate(
            Organization organization,
            Role role,
            Department department,
            Designation designation,
            JobLevel jobLevel,
            EmploymentType employmentType,
            User reportingManager,
            Long currentUserId,
            List<Long> managerChainIds
    ) {
        // 1. Organization ACTIVE
        if (organization == null) {
            throw new BadRequestException("Organization is required");
        }
        if (!"ACTIVE".equals(organization.getStatus().name())) {
            throw new BadRequestException("Organization is not active");
        }

        // 2. Role ACTIVE + same organization (or platform template)
        if (role == null) {
            throw new BadRequestException("Role is required");
        }
        if (!"ACTIVE".equalsIgnoreCase(role.getStatus())) {
            throw new BadRequestException("Role is not active");
        }
        if (!role.isPlatformTemplate()) {
            if (role.getOrganization() == null || !role.getOrganization().getId().equals(organization.getId())) {
                throw new BadRequestException("Role does not belong to the user's organization");
            }
        }

        // 3. Department ACTIVE + same organization
        if (department == null) {
            throw new BadRequestException("Department is required");
        }
        if (!"ACTIVE".equalsIgnoreCase(department.getStatus())) {
            throw new BadRequestException("Department is not active");
        }
        if (department.getOrganization() == null || !department.getOrganization().getId().equals(organization.getId())) {
            throw new BadRequestException("Department does not belong to the user's organization");
        }

        // 4. Designation ACTIVE + same organization
        if (designation == null) {
            throw new BadRequestException("Designation is required");
        }
        if (!"ACTIVE".equalsIgnoreCase(designation.getStatus())) {
            throw new BadRequestException("Designation is not active");
        }
        if (designation.getOrganization() == null || !designation.getOrganization().getId().equals(organization.getId())) {
            throw new BadRequestException("Designation does not belong to the user's organization");
        }

        // 5. JobLevel ACTIVE
        if (jobLevel == null) {
            throw new BadRequestException("Job Level is required");
        }
        if (!"ACTIVE".equalsIgnoreCase(jobLevel.getStatus())) {
            throw new BadRequestException("Job Level is not active");
        }

        // 6. JobLevel belongs to Designation
        if (jobLevel.getDesignation() == null || !jobLevel.getDesignation().getId().equals(designation.getId())) {
            throw new BadRequestException("Job Level does not belong to the specified designation");
        }

        // 7. EmploymentType ACTIVE
        if (employmentType == null) {
            throw new BadRequestException("Employment Type is required");
        }
        if (!"ACTIVE".equalsIgnoreCase(employmentType.getStatus())) {
            throw new BadRequestException("Employment Type is not active");
        }

        // 8. EmploymentType belongs to JobLevel
        if (employmentType.getJobLevel() == null || !employmentType.getJobLevel().getId().equals(jobLevel.getId())) {
            throw new BadRequestException("Employment Type does not belong to the specified job level");
        }

        // 9. Manager ACTIVE + same organization
        if (reportingManager != null) {
            if (!"ACTIVE".equalsIgnoreCase(reportingManager.getStatus())) {
                throw new BadRequestException("Reporting Manager is not active");
            }
            if (reportingManager.getOrganizationId() == null || !reportingManager.getOrganizationId().equals(organization.getId())) {
                throw new BadRequestException("Reporting Manager does not belong to the same organization");
            }
            // 10. Manager != current user
            if (currentUserId != null && currentUserId.equals(reportingManager.getId())) {
                throw new BadRequestException("User cannot report to themselves");
            }
            // 11. No circular reporting chain
            if (currentUserId != null && managerChainIds != null && managerChainIds.contains(currentUserId)) {
                throw new BadRequestException("Circular reporting chain detected");
            }
        }
    }
}
