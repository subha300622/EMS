package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.EmployeePaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePaymentAccountRepository extends JpaRepository<EmployeePaymentAccount, Long> {

    Optional<EmployeePaymentAccount> findByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);

    Optional<EmployeePaymentAccount> findByEmployeeId(Long employeeId);

    List<EmployeePaymentAccount> findByOrganizationId(Long organizationId);

    boolean existsByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);
}
