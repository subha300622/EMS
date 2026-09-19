package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveAccrualRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveAccrualRuleRepository extends JpaRepository<LeaveAccrualRule, Long> {
    Optional<LeaveAccrualRule> findByLeaveTypeIdAndOrganizationId(Long leaveTypeId, Long organizationId);
    Optional<LeaveAccrualRule> findByLeaveTypeId(Long leaveTypeId);
    List<LeaveAccrualRule> findByOrganizationId(Long organizationId);
}
