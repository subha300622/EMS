package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalCycleRepository extends JpaRepository<AppraisalCycle, Long> {
    Optional<AppraisalCycle> findByName(String name);
    List<AppraisalCycle> findByStatus(String status);
    List<AppraisalCycle> findByOrganizationId(Long organizationId);
    Optional<AppraisalCycle> findByIdAndOrganizationId(Long id, Long organizationId);
}
