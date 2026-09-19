package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalConfigurationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalConfigurationVersionRepository extends JpaRepository<AppraisalConfigurationVersion, Long> {
    List<AppraisalConfigurationVersion> findByOrganizationIdOrderByVersionNumberDesc(Long organizationId);
    Optional<AppraisalConfigurationVersion> findByOrganizationIdAndVersionNumber(Long organizationId, Integer versionNumber);
    Optional<AppraisalConfigurationVersion> findFirstByOrganizationIdOrderByVersionNumberDesc(Long organizationId);
}
