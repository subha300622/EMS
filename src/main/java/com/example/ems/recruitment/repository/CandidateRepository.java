package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    List<Candidate> findByOrganizationId(Long organizationId);

    Optional<Candidate> findByOrganizationIdAndId(Long organizationId, Long id);

    Optional<Candidate> findByOrganizationIdAndEmail(Long organizationId, String email);

    Optional<Candidate> findByOrganizationIdAndPhone(Long organizationId, String phone);

    @Query("SELECT c FROM Candidate c WHERE c.organizationId = :orgId AND (c.email = :email OR (c.phone IS NOT NULL AND c.phone = :phone))")
    Optional<Candidate> findByOrganizationIdAndEmailOrPhone(@Param("orgId") Long orgId, @Param("email") String email, @Param("phone") String phone);
}
