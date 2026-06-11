package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    java.util.List<Employee> findByDepartment(String department);

    java.util.List<Employee> findByManagerId(Long managerId);
}
