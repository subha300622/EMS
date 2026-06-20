package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.FnfSettlementAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FnfSettlementAuditRepository extends JpaRepository<FnfSettlementAudit, Long> {
    List<FnfSettlementAudit> findBySettlementIdOrderByUpdatedAtAsc(Long settlementId);
}
