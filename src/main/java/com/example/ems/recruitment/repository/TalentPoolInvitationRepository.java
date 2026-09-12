package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.TalentPoolInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TalentPoolInvitationRepository extends JpaRepository<TalentPoolInvitation, Long> {

    Optional<TalentPoolInvitation> findByOrganizationIdAndCandidateIdAndJobId(Long organizationId, Long candidateId, Long jobId);

    boolean existsByOrganizationIdAndCandidateIdAndJobId(Long organizationId, Long candidateId, Long jobId);
}
