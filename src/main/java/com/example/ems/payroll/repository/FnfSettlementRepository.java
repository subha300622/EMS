package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.FnfSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FnfSettlementRepository extends JpaRepository<FnfSettlement, Long> {
    Optional<FnfSettlement> findByEmployeeId(Long employeeId);
    List<FnfSettlement> findByStatus(String status);
}
