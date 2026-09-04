package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.PayrollPayment;
import com.example.ems.payroll.entity.PayrollPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {

    Optional<PayrollPayment> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<PayrollPayment> findByIdempotencyKey(String idempotencyKey);

    Optional<PayrollPayment> findByPayoutId(String payoutId);

    List<PayrollPayment> findByPayrollRunIdAndOrganizationId(Long payrollRunId, Long organizationId);

    List<PayrollPayment> findByPayrollRunIdAndStatus(Long payrollRunId, PayrollPaymentStatus status);

    List<PayrollPayment> findByEmployeeIdAndOrganizationIdOrderByCreatedAtDesc(Long employeeId, Long organizationId);
}
