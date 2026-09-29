package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.Increment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncrementRepository extends JpaRepository<Increment, Long> {
    List<Increment> findByEmployeeId(Long employeeId);
    List<Increment> findByStatus(String status);
    Page<Increment> findByStatus(String status, Pageable pageable);
    List<Increment> findByEmployeeEmail(String email);

    Optional<Increment> findByAppraisalId(Long appraisalId);

    @Query("SELECT i FROM Increment i WHERE i.employee.organization.id = :orgId ORDER BY i.createdAt DESC")
    List<Increment> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT i FROM Increment i WHERE i.employee.id = :employeeId AND i.employee.organization.id = :orgId ORDER BY i.createdAt DESC")
    List<Increment> findByEmployeeIdAndOrganizationId(@Param("employeeId") Long employeeId, @Param("orgId") Long orgId);

    @Query("SELECT i FROM Increment i WHERE i.id = :id AND i.employee.organization.id = :orgId")
    Optional<Increment> findByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);
}

