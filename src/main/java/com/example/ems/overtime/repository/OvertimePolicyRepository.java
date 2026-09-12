package com.example.ems.overtime.repository;

import com.example.ems.overtime.entity.OvertimePolicy;
import com.example.ems.overtime.entity.OvertimePolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OvertimePolicyRepository extends JpaRepository<OvertimePolicy, Long>, JpaSpecificationExecutor<OvertimePolicy> {

    Optional<OvertimePolicy> findByIdAndOrganizationId(Long id, Long organizationId);

    List<OvertimePolicy> findByOrganizationIdAndStatus(Long organizationId, OvertimePolicyStatus status);

    Page<OvertimePolicy> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<OvertimePolicy> findByOrganizationIdAndStatus(Long organizationId, OvertimePolicyStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM OvertimePolicy p
        WHERE p.organization.id = :organizationId
          AND p.status = 'ACTIVE'
          AND p.effectiveFrom <= :targetDate
          AND (p.effectiveTo IS NULL OR p.effectiveTo >= :targetDate)
        ORDER BY p.id DESC
    """)
    List<OvertimePolicy> findActivePoliciesForDate(
            @Param("organizationId") Long organizationId,
            @Param("targetDate") LocalDate targetDate
    );

    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    boolean existsByOrganizationIdAndNameAndIdNot(Long organizationId, String name, Long id);
}
