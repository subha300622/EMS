package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveBalanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveBalanceAdjustmentRepository extends JpaRepository<LeaveBalanceAdjustment, Long> {
    List<LeaveBalanceAdjustment> findByEmployeeId(Long employeeId);
    List<LeaveBalanceAdjustment> findByOrganizationId(Long organizationId);
}
