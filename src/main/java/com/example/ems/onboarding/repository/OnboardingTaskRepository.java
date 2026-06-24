package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByOnboardingId(Long onboardingId);
    List<OnboardingTask> findByOnboardingIdAndPhase(Long onboardingId, String phase);
}
