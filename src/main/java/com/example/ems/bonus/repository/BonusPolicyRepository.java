package com.example.ems.bonus.repository;

import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusPolicyStatus;
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
public interface BonusPolicyRepository extends JpaRepository<BonusPolicy, Long> {

    Optional<BonusPolicy> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<BonusPolicy> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<BonusPolicy> findByOrganizationIdAndStatus(Long organizationId, BonusPolicyStatus status, Pageable pageable);

    @Query("SELECT p FROM BonusPolicy p WHERE p.organization.id = :orgId " +
           "AND p.status = com.example.ems.bonus.entity.BonusPolicyStatus.ACTIVE " +
           "AND p.effectiveFrom <= :date " +
           "AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date)")
    List<BonusPolicy> findActivePoliciesForDate(@Param("orgId") Long orgId, @Param("date") LocalDate date);

    boolean existsByOrganizationIdAndNameIgnoreCase(Long orgId, String name);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(Long orgId, String name, Long id);
}
