package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingApprovalRepository extends JpaRepository<OnboardingApproval, Long> {
    List<OnboardingApproval> findByOnboardingIdOrderByLevelAsc(Long onboardingId);
}
