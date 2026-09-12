package com.example.ems.employee.mapper;

import com.example.ems.employee.dto.EmployeeListItemDto;
import com.example.ems.employee.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeListItemDto toListItemDto(Employee employee, Employee manager) {
        if (employee == null) return null;
        return new EmployeeListItemDto(
                employee.getId(),
                employee.getEmployeeId(),
                employee.getOrganization() != null ? employee.getOrganization().getId() : null,
                employee.getFullName(),
                employee.getDesignation(),
                employee.getDepartment(),
                employee.getStatus(),
                employee.getWorkMode(),
                manager != null ? manager.getId() : null,
                manager != null ? manager.getFullName() : "Unassigned"
        );
    }
}
