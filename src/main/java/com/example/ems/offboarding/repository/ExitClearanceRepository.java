package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitClearance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExitClearanceRepository extends JpaRepository<ExitClearance, Long> {

    Optional<ExitClearance> findByIdAndOrganizationId(Long id, Long organizationId);

    List<ExitClearance> findByExitIdAndOrganizationId(Long exitId, Long organizationId);

    List<ExitClearance> findByExitId(Long exitId);

    Optional<ExitClearance> findByExitIdAndDepartment(Long exitId, String department);

    List<ExitClearance> findByAssignedToIdAndStatus(Long assignedToId, String status);

    List<ExitClearance> findByOrganizationIdAndStatus(Long organizationId, String status);
}
