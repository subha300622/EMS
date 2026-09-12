package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnboardingAuditLogRepository extends JpaRepository<OnboardingAuditLog, Long> {
    Page<OnboardingAuditLog> findByOnboardingId(Long onboardingId, Pageable pageable);
}
