package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingClearanceTaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingClearanceTaskTemplateRepository extends JpaRepository<OffboardingClearanceTaskTemplate, Long> {

    List<OffboardingClearanceTaskTemplate> findByTemplateIdAndOrganizationIdOrderBySequenceAsc(Long templateId, Long organizationId);

    List<OffboardingClearanceTaskTemplate> findByTemplateIdAndOrganizationIdAndActiveTrueOrderBySequenceAsc(Long templateId, Long organizationId);

    Optional<OffboardingClearanceTaskTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByTemplateIdAndOrganizationId(Long templateId, Long organizationId);
}
