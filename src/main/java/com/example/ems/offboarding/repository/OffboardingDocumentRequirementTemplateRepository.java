package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingDocumentRequirementTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingDocumentRequirementTemplateRepository extends JpaRepository<OffboardingDocumentRequirementTemplate, Long> {

    List<OffboardingDocumentRequirementTemplate> findByTemplateIdAndOrganizationIdOrderBySequenceAsc(Long templateId, Long organizationId);

    List<OffboardingDocumentRequirementTemplate> findByTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(Long templateId, Long organizationId);

    Optional<OffboardingDocumentRequirementTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByTemplateIdAndOrganizationId(Long templateId, Long organizationId);
}
