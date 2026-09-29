package com.example.ems.employee.service.actioncenter;

import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.entity.Employee;

import java.util.List;

public interface EmployeeActionProvider {
    List<EmployeeActionDto> getActions(Employee employee, Long orgId);
}
