package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingEventLogRepository extends JpaRepository<OnboardingEventLog, Long> {
    List<OnboardingEventLog> findByOnboardingIdOrderByTimestampDesc(Long onboardingId);
    List<OnboardingEventLog> findByStatusOrderByTimestampDesc(String status);
}
