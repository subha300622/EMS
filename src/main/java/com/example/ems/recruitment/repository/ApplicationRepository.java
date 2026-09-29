package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Application;
import com.example.ems.recruitment.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {

    Optional<Application> findByOrganizationIdAndId(Long organizationId, Long id);

    Optional<Application> findByOrganizationIdAndCandidateIdAndJobId(Long organizationId, Long candidateId, Long jobId);

    boolean existsByOrganizationIdAndCandidateIdAndJobId(Long organizationId, Long candidateId, Long jobId);

    long countByOrganizationId(Long organizationId);

    long countByOrganizationIdAndStatus(Long organizationId, ApplicationStatus status);

    List<Application> findByOrganizationIdAndCandidateId(Long organizationId, Long candidateId);

    List<Application> findByOrganizationIdAndJobId(Long organizationId, Long jobId);

    boolean existsByOrganizationIdAndJobId(Long organizationId, Long jobId);
}
