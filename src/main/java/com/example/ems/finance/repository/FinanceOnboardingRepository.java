package com.example.ems.finance.repository;

import com.example.ems.finance.entity.FinanceOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceOnboardingRepository extends JpaRepository<FinanceOnboarding, Long> {
    Optional<FinanceOnboarding> findFirstByStatusOrderByCreatedAtDesc(String status);
    List<FinanceOnboarding> findByStatusNot(String status);
}
