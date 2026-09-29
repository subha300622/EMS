package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.PayrollItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    List<PayrollItem> findByPayrollEmployeeIdAndOrganizationIdOrderByIdAsc(Long payrollEmployeeId, Long organizationId);

    void deleteByPayrollEmployeeIdAndOrganizationId(Long payrollEmployeeId, Long organizationId);
}
