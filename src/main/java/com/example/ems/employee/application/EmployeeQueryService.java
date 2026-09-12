package com.example.ems.employee.application;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeQueryService {

    @Autowired
    private EmployeeService employeeService;

    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeService.getEmployeeById(id);
    }

    public List<Employee> searchEmployees(String query) {
        return employeeService.searchEmployees(query);
    }

    public Map<String, Object> getEmployeeMasterProfileData(String identifier) {
        return employeeService.getEmployeeMasterProfileData(identifier);
    }

    public Map<String, Object> getEmployeeStatusDetail(String identifier) {
        return employeeService.getEmployeeStatusDetail(identifier);
    }
}
