package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitKtPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExitKtPlanRepository extends JpaRepository<ExitKtPlan, Long> {
    Optional<ExitKtPlan> findByEmployeeId(Long employeeId);
}
