package com.example.ems.employee.repository;

import com.example.ems.employee.entity.JobLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface JobLevelRepository extends JpaRepository<JobLevel, Long> {
    List<JobLevel> findByDesignationId(Long designationId);
    Optional<JobLevel> findByIdAndDesignationId(Long id, Long designationId);

    List<JobLevel> findByDesignationIdAndOrganizationId(Long designationId, Long organizationId);
    Optional<JobLevel> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<JobLevel> findByIdAndDesignationIdAndOrganizationId(Long id, Long designationId, Long organizationId);
    @Query("SELECT COUNT(jl) > 0 FROM JobLevel jl WHERE jl.designation.id = :designationId AND LOWER(jl.jobLevel) = LOWER(:jobLevel) AND jl.organization.id = :organizationId")
    boolean existsByJobLevelIgnoreCaseAndDesignationIdAndOrganizationId(@Param("jobLevel") String jobLevel, @Param("designationId") Long designationId, @Param("organizationId") Long organizationId);
}
