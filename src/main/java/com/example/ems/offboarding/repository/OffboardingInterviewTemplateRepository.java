package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingInterviewTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OffboardingInterviewTemplateRepository extends JpaRepository<OffboardingInterviewTemplate, Long> {

    Optional<OffboardingInterviewTemplate> findByTemplateIdAndOrganizationId(Long templateId, Long organizationId);

    Optional<OffboardingInterviewTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByTemplateIdAndOrganizationId(Long templateId, Long organizationId);
}
