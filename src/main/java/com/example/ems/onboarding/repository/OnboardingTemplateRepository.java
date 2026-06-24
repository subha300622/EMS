package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingTemplateRepository extends JpaRepository<OnboardingTemplate, Long> {
    Optional<OnboardingTemplate> findByIsActiveTrue();
}
