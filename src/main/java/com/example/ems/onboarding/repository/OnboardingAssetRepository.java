package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingAsset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingAssetRepository extends JpaRepository<OnboardingAsset, Long> {
    List<OnboardingAsset> findByOnboardingId(Long onboardingId);
}
