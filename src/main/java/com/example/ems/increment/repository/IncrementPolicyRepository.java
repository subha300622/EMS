package com.example.ems.increment.repository;

import com.example.ems.increment.entity.IncrementPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("enterpriseIncrementPolicyRepository")
public interface IncrementPolicyRepository extends JpaRepository<IncrementPolicy, Long> {

    @Query("SELECT p FROM EnterpriseIncrementPolicy p WHERE p.organization.id = :orgId AND p.active = true ORDER BY p.version DESC")
    List<IncrementPolicy> findActivePoliciesByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT p FROM EnterpriseIncrementPolicy p WHERE p.organization.id = :orgId AND p.active = true ORDER BY p.version DESC")
    Optional<IncrementPolicy> findFirstActiveByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT p FROM EnterpriseIncrementPolicy p WHERE p.id = :id AND p.organization.id = :orgId")
    Optional<IncrementPolicy> findByIdAndOrgId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT MAX(p.version) FROM EnterpriseIncrementPolicy p WHERE p.organization.id = :orgId")
    Integer findMaxVersionByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT p FROM EnterpriseIncrementPolicy p WHERE p.organization.id = :orgId ORDER BY p.version DESC")
    List<IncrementPolicy> findAllByOrgId(@Param("orgId") Long orgId);
}
