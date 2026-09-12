package com.example.ems.leave.repository;

import com.example.ems.leave.entity.EmployeeLeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeavePolicyRepository extends JpaRepository<EmployeeLeavePolicy, Long> {
    List<EmployeeLeavePolicy> findByEmployeeIdAndActiveTrue(Long employeeId);
    List<EmployeeLeavePolicy> findByLeavePolicyIdAndActiveTrue(Long leavePolicyId);
    Optional<EmployeeLeavePolicy> findByEmployeeIdAndLeavePolicyId(Long employeeId, Long leavePolicyId);
    List<EmployeeLeavePolicy> findByOrganizationId(Long organizationId);
}
