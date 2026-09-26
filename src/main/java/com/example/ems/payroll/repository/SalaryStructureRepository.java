package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.entity.SalaryStructureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByOrganizationId(Long organizationId);

    List<SalaryStructure> findByOrganizationIdAndStatus(Long organizationId, SalaryStructureStatus status);

    List<SalaryStructure> findByOrganizationIdAndCode(Long organizationId, String code);

    Optional<SalaryStructure> findByOrganizationIdAndCodeAndVersion(Long organizationId, String code, Integer version);

    Optional<SalaryStructure> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<SalaryStructure> findTopByOrganizationIdAndCodeOrderByVersionDesc(Long organizationId, String code);

    boolean existsByOrganizationIdAndCodeAndVersion(Long organizationId, String code, Integer version);

    @Query("SELECT s FROM SalaryStructure s WHERE s.organizationId = :orgId " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<SalaryStructure> searchStructures(
            @Param("orgId") Long organizationId,
            @Param("status") SalaryStructureStatus status,
            @Param("search") String search);

    // Retained for backward compatibility with legacy EmployeeFinanceOnboarding until Batch 5
    Optional<SalaryStructure> findByEmployeeId(Long employeeId);
}
