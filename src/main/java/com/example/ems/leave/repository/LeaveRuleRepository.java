package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRuleRepository extends JpaRepository<LeaveRule, Long> {
    Optional<LeaveRule> findByLeaveTypeIdAndOrganizationId(Long leaveTypeId, Long organizationId);
    Optional<LeaveRule> findByLeaveTypeId(Long leaveTypeId);
    List<LeaveRule> findByOrganizationId(Long organizationId);
}
