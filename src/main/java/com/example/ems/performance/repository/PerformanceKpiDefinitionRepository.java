package com.example.ems.performance.repository;

import com.example.ems.performance.entity.PerformanceKpiDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceKpiDefinitionRepository extends JpaRepository<PerformanceKpiDefinition, Long> {

    Optional<PerformanceKpiDefinition> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PerformanceKpiDefinition> findByOrganizationIdAndCycleId(Long organizationId, Long cycleId);

    List<PerformanceKpiDefinition> findByOrganizationIdAndCycleIdAndStatus(Long organizationId, Long cycleId, String status);

    Optional<PerformanceKpiDefinition> findByOrganizationIdAndCycleIdAndCode(Long organizationId, Long cycleId, String code);

    List<PerformanceKpiDefinition> findByOrganizationId(Long organizationId);
}
