package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.EmployeeSalaryComponentValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSalaryComponentValueRepository extends JpaRepository<EmployeeSalaryComponentValue, Long> {

    @Query("SELECT v FROM EmployeeSalaryComponentValue v " +
           "JOIN FETCH v.salaryComponent sc " +
           "WHERE v.salaryAssignment.id = :assignmentId " +
           "ORDER BY v.id ASC")
    List<EmployeeSalaryComponentValue> findBySalaryAssignmentId(@Param("assignmentId") Long assignmentId);

    Optional<EmployeeSalaryComponentValue> findByIdAndSalaryAssignmentId(Long id, Long salaryAssignmentId);

    Optional<EmployeeSalaryComponentValue> findBySalaryAssignmentIdAndSalaryComponentId(Long salaryAssignmentId, Long salaryComponentId);

    boolean existsBySalaryAssignmentIdAndSalaryComponentId(Long salaryAssignmentId, Long salaryComponentId);

    boolean existsBySalaryAssignmentIdAndSalaryComponentIdAndIdNot(Long salaryAssignmentId, Long salaryComponentId, Long id);

    void deleteBySalaryAssignmentId(Long salaryAssignmentId);
}
