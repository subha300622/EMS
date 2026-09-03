package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.PayrollEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollEmployeeRepository extends JpaRepository<PayrollEmployee, Long> {

    Optional<PayrollEmployee> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PayrollEmployee> findByPayrollRunIdAndOrganizationIdOrderByIdAsc(Long payrollRunId, Long organizationId);

    Optional<PayrollEmployee> findByPayrollRunIdAndEmployeeIdAndOrganizationId(Long payrollRunId, Long employeeId, Long organizationId);

    List<PayrollEmployee> findByEmployeeIdAndOrganizationIdOrderByCalculationDateDesc(Long employeeId, Long organizationId);

    void deleteByPayrollRunIdAndOrganizationId(Long payrollRunId, Long organizationId);
}
