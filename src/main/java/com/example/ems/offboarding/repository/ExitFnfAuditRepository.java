package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitFnfAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitFnfAuditRepository extends JpaRepository<ExitFnfAudit, Long> {
    List<ExitFnfAudit> findBySettlementIdOrderByCreatedAtDesc(Long settlementId);
    List<ExitFnfAudit> findBySettlementIdAndOrganizationIdOrderByCreatedAtDesc(Long settlementId, Long organizationId);
}
