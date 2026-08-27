package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveAccrualTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveAccrualTransactionRepository extends JpaRepository<LeaveAccrualTransaction, Long> {
    List<LeaveAccrualTransaction> findByEmployeeId(Long employeeId);
    List<LeaveAccrualTransaction> findByOrganizationId(Long organizationId);
}
