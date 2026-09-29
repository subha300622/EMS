package com.example.ems.incentive.repository;

import com.example.ems.incentive.entity.IncentivePolicy;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncentivePolicyRepository extends JpaRepository<IncentivePolicy, Long> {

    Optional<IncentivePolicy> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<IncentivePolicy> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<IncentivePolicy> findByOrganizationIdAndStatus(Long organizationId, IncentivePolicyStatus status, Pageable pageable);

    @Query("SELECT p FROM IncentivePolicy p WHERE p.organization.id = :orgId " +
           "AND p.status = com.example.ems.incentive.entity.IncentivePolicyStatus.ACTIVE " +
           "AND p.effectiveFrom <= :date " +
           "AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date)")
    List<IncentivePolicy> findActivePoliciesForDate(@Param("orgId") Long orgId, @Param("date") LocalDate date);

    boolean existsByOrganizationIdAndNameIgnoreCase(Long orgId, String name);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(Long orgId, String name, Long id);
}
