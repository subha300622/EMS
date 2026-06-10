package com.example.ems.repository;

import com.example.ems.entity.OnboardingAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingAssetRepository extends JpaRepository<OnboardingAsset, Long> {
    List<OnboardingAsset> findByOnboardingId(Long onboardingId);
}
