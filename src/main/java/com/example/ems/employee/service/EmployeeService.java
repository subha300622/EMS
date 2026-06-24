package com.example.ems.employee.service;

import com.example.ems.employee.dto.EmployeeRequest;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import com.example.ems.employee.event.EmployeeCreatedEvent;

import java.util.List;
import java.util.Optional;

import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DepartmentTransferRepository;
import com.example.ems.appraisal.repository.IncrementRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentTransferRepository departmentTransferRepository;

    @Autowired
    private IncrementRepository incrementRepository;

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

        Employee saved = employeeRepository.save(employee);
        eventPublisher.publishEvent(new EmployeeCreatedEvent(this, saved));
        return saved;
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

    public List<Employee> searchEmployees(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllEmployees();
        }
        String q = query.trim().toLowerCase();
        return employeeRepository.findAll().stream()
                .filter(e -> e.getFullName().toLowerCase().contains(q)
                        || e.getEmail().toLowerCase().contains(q)
                        || (e.getDepartment() != null && e.getDepartment().toLowerCase().contains(q))
                        || (e.getLocation() != null && e.getLocation().toLowerCase().contains(q)))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Optional<Employee> updateEmployeeStatus(Long id, String status) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setStatus(status);
            return employeeRepository.save(employee);
        });
    }

    public List<java.util.Map<String, Object>> getEmployeeTimeline(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        List<java.util.Map<String, Object>> timeline = new java.util.ArrayList<>();

        // 1. Joined event
        if (employee.getJoiningDate() != null) {
            java.util.Map<String, Object> joined = new java.util.LinkedHashMap<>();
            joined.put("date", employee.getJoiningDate().toString());
            joined.put("type", "JOINED");
            joined.put("title", "Joined Company");
            joined.put("description", "Joined the company as " + employee.getDesignation() + " in " + employee.getDepartment() + " department.");
            timeline.add(joined);
        }

        // 2. Department transfer events
        List<com.example.ems.employee.entity.DepartmentTransfer> transfers = departmentTransferRepository.findByEmployeeId(employeeId);
        for (com.example.ems.employee.entity.DepartmentTransfer transfer : transfers) {
            String fromName = "Unknown";
            String toName = "Unknown";
            if (transfer.getFromDepartmentId() != null) {
                fromName = departmentRepository.findById(transfer.getFromDepartmentId())
                        .map(com.example.ems.employee.entity.Department::getName)
                        .orElse("Unknown");
            }
            if (transfer.getToDepartmentId() != null) {
                toName = departmentRepository.findById(transfer.getToDepartmentId())
                        .map(com.example.ems.employee.entity.Department::getName)
                        .orElse("Unknown");
            }

            java.util.Map<String, Object> event = new java.util.LinkedHashMap<>();
            event.put("date", transfer.getEffectiveDate() != null ? transfer.getEffectiveDate().toString() : transfer.getTransferDate().toLocalDate().toString());
            event.put("type", "DEPARTMENT_TRANSFER");
            event.put("title", "Department Transfer");
            event.put("description", "Transferred from department '" + fromName + "' to '" + toName + "'. Remarks: " + transfer.getRemarks());
            timeline.add(event);
        }

        // 3. Salary Revision (Increment) events
        List<com.example.ems.appraisal.entity.Increment> increments = incrementRepository.findByEmployeeId(employeeId);
        for (com.example.ems.appraisal.entity.Increment inc : increments) {
            if ("APPROVED".equalsIgnoreCase(inc.getStatus()) || "APPLIED".equalsIgnoreCase(inc.getStatus())) {
                java.util.Map<String, Object> event = new java.util.LinkedHashMap<>();
                event.put("date", inc.getEffectiveDate() != null ? inc.getEffectiveDate().toString() : inc.getCreatedAt().toLocalDate().toString());
                event.put("type", "SALARY_REVISION");
                event.put("title", "Salary Revision");
                event.put("description", "Salary revised from " + inc.getCurrentSalary() + " to " + inc.getNewSalary() + ". Reason: " + inc.getReason());
                timeline.add(event);
            }
        }

        // Sort chronological
        timeline.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));

        return timeline;
    }

    @Transactional
    public List<Employee> importEmployees(List<EmployeeRequest> requests) {
        List<Employee> imported = new java.util.ArrayList<>();
        for (EmployeeRequest req : requests) {
            imported.add(createEmployee(req));
        }
        return imported;
    }
}

