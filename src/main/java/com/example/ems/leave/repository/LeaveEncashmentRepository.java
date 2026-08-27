package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveEncashment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveEncashmentRepository extends JpaRepository<LeaveEncashment, Long> {
    List<LeaveEncashment> findByEmployeeId(Long employeeId);
    List<LeaveEncashment> findByOrganizationId(Long organizationId);
}
