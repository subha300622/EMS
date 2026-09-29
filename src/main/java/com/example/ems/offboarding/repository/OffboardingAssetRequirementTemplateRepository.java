package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingAssetRequirementTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingAssetRequirementTemplateRepository extends JpaRepository<OffboardingAssetRequirementTemplate, Long> {

    List<OffboardingAssetRequirementTemplate> findByTemplateIdAndOrganizationIdOrderBySequenceAsc(Long templateId, Long organizationId);

    List<OffboardingAssetRequirementTemplate> findByTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(Long templateId, Long organizationId);

    Optional<OffboardingAssetRequirementTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByTemplateIdAndOrganizationId(Long templateId, Long organizationId);
}
