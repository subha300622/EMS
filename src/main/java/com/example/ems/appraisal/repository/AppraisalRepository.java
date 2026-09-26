package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {
    List<Appraisal> findByEmployeeId(Long employeeId);
    List<Appraisal> findByOrganizationId(Long organizationId);
    List<Appraisal> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);
    Optional<Appraisal> findByIdAndOrganizationId(Long id, Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appraisal a WHERE a.id = :id AND a.organization.id = :organizationId")
    Optional<Appraisal> findByIdAndOrganizationIdForUpdate(@Param("id") Long id, @Param("organizationId") Long organizationId);

    List<Appraisal> findByCycleId(Long cycleId);
    Page<Appraisal> findByCycleId(Long cycleId, Pageable pageable);
    List<Appraisal> findByStatus(AppraisalStatus status);
    Page<Appraisal> findByStatus(AppraisalStatus status, Pageable pageable);
    Optional<Appraisal> findByEmployeeEmailAndCycleId(String email, Long cycleId);
    List<Appraisal> findByEmployeeEmail(String email);
    Optional<Appraisal> findByRequestId(Long requestId);

    @Query("SELECT a FROM Appraisal a WHERE " +
           "(:managerId IS NULL OR a.employee.manager.id = :managerId) AND " +
           "(:cycleId IS NULL OR a.cycle.id = :cycleId) AND " +
           "(:status IS NULL OR a.status = :status)")
    List<Appraisal> findTeamAppraisals(
            @Param("managerId") Long managerId,
            @Param("cycleId") Long cycleId,
            @Param("status") AppraisalStatus status);
}
