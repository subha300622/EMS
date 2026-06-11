package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingSettlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffboardingSettlementRepository extends JpaRepository<OffboardingSettlement, Long> {
    List<OffboardingSettlement> findByOffboardingId(Long offboardingId);
}
