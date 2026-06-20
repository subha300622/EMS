package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.entity.FnfSettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FnfSettlementRepository extends JpaRepository<FnfSettlement, Long> {
    List<FnfSettlement> findByEmployeeId(Long employeeId);
    List<FnfSettlement> findByStatus(FnfSettlementStatus status);
}
