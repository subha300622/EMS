package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByOrganizationId(Long organizationId);

    List<Job> findByOrganizationIdAndStatus(Long organizationId, JobStatus status);

    Optional<Job> findByOrganizationIdAndId(Long organizationId, Long id);

    Optional<Job> findByOrganizationIdAndSlug(Long organizationId, String slug);

    Optional<Job> findBySlug(String slug);

    Optional<Job> findBySlugAndStatus(String slug, JobStatus status);

    long countByOrganizationIdAndStatus(Long organizationId, JobStatus status);

    boolean existsByOrganizationIdAndSlug(Long organizationId, String slug);
}
