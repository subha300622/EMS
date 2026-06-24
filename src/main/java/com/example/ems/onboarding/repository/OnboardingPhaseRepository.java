package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingPhaseRepository extends JpaRepository<OnboardingPhase, Long> {
    List<OnboardingPhase> findByOnboardingId(Long onboardingId);
    Optional<OnboardingPhase> findByOnboardingIdAndPhaseName(Long onboardingId, String phaseName);
}
