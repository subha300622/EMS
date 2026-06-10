package com.example.ems.service;

import com.example.ems.dto.EmployeeRequest;
import com.example.ems.entity.Employee;
import com.example.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
        }

        Employee employee = new Employee();
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeId(request.getEmployeeId());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setDob(request.getDob());
        employee.setAddress(request.getAddress());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setAnnualSalary(request.getAnnualSalary());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setLocation(request.getLocation());
        employee.setEmploymentType(request.getEmploymentType());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            employee.setStatus(request.getStatus());
        } else {
            employee.setStatus("ACTIVE");
        }

        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + request.getManagerId()));
            employee.setManager(manager);
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    public Optional<Employee> updateEmployee(Long id, EmployeeRequest request) {
        return employeeRepository.findById(id).map(employee -> {
            if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                    && employeeRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
            }

            if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                    && !request.getEmployeeId().equalsIgnoreCase(employee.getEmployeeId())
                    && employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new IllegalArgumentException("Employee ID '" + request.getEmployeeId() + "' already exists");
            }

            employee.setFullName(request.getFullName());
            employee.setEmail(request.getEmail());
            employee.setEmployeeId(request.getEmployeeId());
            employee.setPhone(request.getPhone());
            employee.setGender(request.getGender());
            employee.setDob(request.getDob());
            employee.setAddress(request.getAddress());
            employee.setDepartment(request.getDepartment());
            employee.setDesignation(request.getDesignation());
            employee.setAnnualSalary(request.getAnnualSalary());
            employee.setJoiningDate(request.getJoiningDate());
            employee.setLocation(request.getLocation());
            employee.setEmploymentType(request.getEmploymentType());
            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                employee.setStatus(request.getStatus());
            }

            if (request.getManagerId() != null) {
                Employee manager = employeeRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new IllegalArgumentException("Manager not found with ID: " + request.getManagerId()));
                employee.setManager(manager);
            } else {
                employee.setManager(null);
            }

            return employeeRepository.save(employee);
        });
    }

    @Transactional
    public boolean deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }
}
