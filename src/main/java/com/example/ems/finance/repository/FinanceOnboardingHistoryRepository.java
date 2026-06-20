package com.example.ems.finance.repository;

import com.example.ems.finance.entity.FinanceOnboardingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinanceOnboardingHistoryRepository extends JpaRepository<FinanceOnboardingHistory, Long> {
    List<FinanceOnboardingHistory> findByOnboardingIdOrderByTimestampDesc(Long onboardingId);
    List<FinanceOnboardingHistory> findTop10ByOrderByTimestampDesc();
}
