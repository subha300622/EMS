package com.example.ems.employee.repository;

import com.example.ems.employee.entity.EmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentTypeRepository extends JpaRepository<EmploymentType, Long> {
    List<EmploymentType> findByJobLevelId(Long jobLevelId);
    Optional<EmploymentType> findByIdAndJobLevelId(Long id, Long jobLevelId);

    List<EmploymentType> findByJobLevelIdAndOrganizationId(Long jobLevelId, Long organizationId);
    Optional<EmploymentType> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<EmploymentType> findByIdAndJobLevelIdAndOrganizationId(Long id, Long jobLevelId, Long organizationId);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(et) > 0 FROM EmploymentType et WHERE et.jobLevel.id = :jobLevelId AND LOWER(et.employmentType) = LOWER(:employmentType) AND et.organization.id = :organizationId")
    boolean existsByEmploymentTypeIgnoreCaseAndJobLevelIdAndOrganizationId(@org.springframework.data.repository.query.Param("employmentType") String employmentType, @org.springframework.data.repository.query.Param("jobLevelId") Long jobLevelId, @org.springframework.data.repository.query.Param("organizationId") Long organizationId);
}
