package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByFullName(String fullName);

    List<Employee> findByDepartment(String department);

    List<Employee> findByManagerId(Long managerId);

    long countByManagerId(Long managerId);

    boolean existsByDesignationIgnoreCase(String designation);

    long countByDesignationIgnoreCase(String designation);

    // ── Tenant-Scoped Organization Isolation Methods ─────────────────────────

    Optional<Employee> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Employee> findByEmployeeIdAndOrganizationId(String employeeId, Long organizationId);

    Optional<Employee> findByEmailAndOrganizationId(String email, Long organizationId);

    List<Employee> findByOrganizationId(Long organizationId);

    List<Employee> findByOrganizationIdAndStatus(Long organizationId, String status);

    List<Employee> findByOrganizationIdAndDepartment(Long organizationId, String department);

    List<Employee> findByOrganizationIdAndManagerId(Long organizationId, Long managerId);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    boolean existsByEmployeeIdAndOrganizationId(String employeeId, Long organizationId);
}
