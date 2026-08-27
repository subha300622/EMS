package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployeeId(Long employeeId);
    List<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, Integer year);
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, Integer year);
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeId(Long employeeId, Long leaveTypeId);
    List<LeaveBalance> findByOrganizationId(Long organizationId);
}
