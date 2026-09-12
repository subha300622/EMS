package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppraisalConfigurationRepository extends JpaRepository<AppraisalConfiguration, Long> {
    Optional<AppraisalConfiguration> findByOrganizationId(Long organizationId);
    Optional<AppraisalConfiguration> findByOrganizationIdAndActiveTrue(Long organizationId);
}
