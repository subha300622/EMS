package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTemplateSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingTemplateSnapshotRepository extends JpaRepository<OnboardingTemplateSnapshot, Long> {
    Optional<OnboardingTemplateSnapshot> findByOnboardingId(Long onboardingId);
}
