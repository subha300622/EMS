package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeavePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
}
