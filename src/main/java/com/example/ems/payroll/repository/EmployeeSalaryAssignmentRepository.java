package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.EmployeeSalaryAssignment;
import com.example.ems.payroll.entity.SalaryAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryAssignmentRepository extends JpaRepository<EmployeeSalaryAssignment, Long> {

    Optional<EmployeeSalaryAssignment> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT a FROM EmployeeSalaryAssignment a " +
           "JOIN FETCH a.salaryStructure ss " +
           "LEFT JOIN FETCH a.componentValues cv " +
           "LEFT JOIN FETCH cv.salaryComponent " +
           "WHERE a.organizationId = :orgId AND a.employee.id = :empId " +
           "ORDER BY a.effectiveFrom DESC, a.id DESC")
    List<EmployeeSalaryAssignment> findByOrganizationIdAndEmployeeIdOrderByEffectiveFromDesc(
            @Param("orgId") Long organizationId,
            @Param("empId") Long employeeId);

    @Query("SELECT a FROM EmployeeSalaryAssignment a " +
           "JOIN FETCH a.salaryStructure ss " +
           "LEFT JOIN FETCH a.componentValues cv " +
           "LEFT JOIN FETCH cv.salaryComponent " +
           "WHERE a.organizationId = :orgId AND a.employee.id = :empId " +
           "AND a.status = 'ACTIVE' " +
           "AND a.effectiveFrom <= :date " +
           "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :date) " +
           "ORDER BY a.effectiveFrom DESC")
    List<EmployeeSalaryAssignment> findActiveAssignmentsForDate(
            @Param("orgId") Long organizationId,
            @Param("empId") Long employeeId,
            @Param("date") LocalDate date);

    Optional<EmployeeSalaryAssignment> findTopByOrganizationIdAndEmployeeIdAndEffectiveToIsNullAndStatusOrderByEffectiveFromDesc(
            Long organizationId,
            Long employeeId,
            SalaryAssignmentStatus status);

    @Query("SELECT a FROM EmployeeSalaryAssignment a " +
           "WHERE a.organizationId = :orgId AND a.employee.id = :empId " +
           "AND a.id <> :excludeId " +
           "AND a.effectiveTo IS NOT NULL " +
           "AND a.effectiveTo >= :newFrom")
    List<EmployeeSalaryAssignment> findClosedAssignmentsFrom(
            @Param("orgId") Long organizationId,
            @Param("empId") Long employeeId,
            @Param("excludeId") Long excludeId,
            @Param("newFrom") LocalDate newFrom);
}
