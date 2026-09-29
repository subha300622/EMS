package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingKtTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OffboardingKtTemplateRepository extends JpaRepository<OffboardingKtTemplate, Long> {

    Optional<OffboardingKtTemplate> findByTemplateIdAndOrganizationId(Long templateId, Long organizationId);

    Optional<OffboardingKtTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    void deleteByTemplateIdAndOrganizationId(Long templateId, Long organizationId);
}
