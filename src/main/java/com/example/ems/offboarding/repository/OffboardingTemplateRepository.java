package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.OffboardingTemplate;
import com.example.ems.offboarding.enums.OffboardingTemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OffboardingTemplateRepository extends JpaRepository<OffboardingTemplate, Long> {

    Optional<OffboardingTemplate> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);

    boolean existsByNameAndOrganizationIdAndIdNot(String name, Long organizationId, Long id);

    Page<OffboardingTemplate> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<OffboardingTemplate> findByOrganizationIdAndStatus(Long organizationId, OffboardingTemplateStatus status, Pageable pageable);

    List<OffboardingTemplate> findByOrganizationIdAndStatus(Long organizationId, OffboardingTemplateStatus status);

    @Query("SELECT t FROM OffboardingTemplate t WHERE t.organizationId = :orgId " +
           "AND (LOWER(t.name) LIKE :pattern OR LOWER(t.description) LIKE :pattern)")
    Page<OffboardingTemplate> findByOrganizationIdAndSearch(
            @Param("orgId") Long orgId,
            @Param("pattern") String pattern,
            Pageable pageable);

    @Query("SELECT t FROM OffboardingTemplate t WHERE t.organizationId = :orgId " +
           "AND t.status = :status " +
           "AND (LOWER(t.name) LIKE :pattern OR LOWER(t.description) LIKE :pattern)")
    Page<OffboardingTemplate> findByOrganizationIdAndStatusAndSearch(
            @Param("orgId") Long orgId,
            @Param("status") OffboardingTemplateStatus status,
            @Param("pattern") String pattern,
            Pageable pageable);
}
