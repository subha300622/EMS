package com.example.ems.employee.application;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class EmployeeCommandService {

    @Autowired
    private EmployeeService employeeService;

    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        return employeeService.deleteEmployee(id);
    }

    @Transactional
    public Map<String, Object> updateEmployeeStatusPatch(String identifier, String status, String reason, User currentUser) {
        return employeeService.updateEmployeeStatusPatch(identifier, status, reason, currentUser);
    }
}
