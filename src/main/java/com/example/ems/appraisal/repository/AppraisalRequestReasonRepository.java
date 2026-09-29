package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalRequestReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRequestReasonRepository extends JpaRepository<AppraisalRequestReason, Long> {
    List<AppraisalRequestReason> findByOrganizationId(Long organizationId);
    List<AppraisalRequestReason> findByOrganizationIdAndActiveTrue(Long organizationId);
    Optional<AppraisalRequestReason> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<AppraisalRequestReason> findByOrganizationIdAndCodeIgnoreCase(Long organizationId, String code);
    boolean existsByOrganizationIdAndCodeIgnoreCase(Long organizationId, String code);
}
