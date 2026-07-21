package com.example.ems.onboarding.repository;

import com.example.ems.onboarding.entity.OnboardingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnboardingTemplateRepository extends JpaRepository<OnboardingTemplate, Long> {
    Optional<OnboardingTemplate> findByIsActiveTrue();

    long countByDepartmentId(String departmentId);

    Optional<OnboardingTemplate> findByTemplateCode(String templateCode);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE OnboardingTemplate t SET t.isDefault = false WHERE t.departmentId = :departmentId AND t.designation = :designation AND t.employmentType = :employmentType AND t.isDefault = true AND t.status = 'ACTIVE'")
    void resetDefaultTemplate(String departmentId, String designation, String employmentType);
}
