package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.Payslip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    Optional<Payslip> findByPayrollId(Long payrollId);
    List<Payslip> findByPayrollEmployeeId(Long employeeId);
}
