package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTemplateTaskSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingTemplateTaskSnapshotRepository extends JpaRepository<OnboardingTemplateTaskSnapshot, Long> {
    List<OnboardingTemplateTaskSnapshot> findBySnapshotId(Long snapshotId);
}
