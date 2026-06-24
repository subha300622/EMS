package com.example.ems.attendance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TeamResolutionService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Employee> resolveEmployees(String scope, Long employeeId, Long managerId, Long departmentId, User currentUser) {
        if (scope == null) {
            scope = "global";
        }
        scope = scope.toLowerCase().trim();

        switch (scope) {
            case "employee":
                if (employeeId == null) {
                    throw new IllegalArgumentException("Employee ID is required for employee scope");
                }
                return employeeRepository.findById(employeeId)
                        .map(List::of)
                        .orElse(Collections.emptyList());

            case "manager":
                Long targetManagerId = managerId;
                if (targetManagerId == null) {
                    if (currentUser != null) {
                        Optional<Employee> currentEmp = employeeRepository.findByEmail(currentUser.getWorkEmail());
                        if (currentEmp.isPresent()) {
                            targetManagerId = currentEmp.get().getId();
                        }
                    }
                }
                if (targetManagerId == null) {
                    throw new IllegalArgumentException("Manager ID is required for manager scope");
                }
                return employeeRepository.findByManagerId(targetManagerId);

            case "department":
                if (departmentId == null) {
                    throw new IllegalArgumentException("Department ID is required for department scope");
                }
                Optional<Department> dept = departmentRepository.findById(departmentId);
                if (dept.isEmpty()) {
                    return Collections.emptyList();
                }
                return employeeRepository.findByDepartment(dept.get().getName());

            case "global":
            default:
                return employeeRepository.findAll();
        }
    }
}
