package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.EmployeeExit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeExitRepository
        extends JpaRepository<EmployeeExit, Long>, JpaSpecificationExecutor<EmployeeExit> {

    Optional<EmployeeExit> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<EmployeeExit> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<EmployeeExit> findByOrganizationIdAndStatus(Long organizationId, String status, Pageable pageable);

    List<EmployeeExit> findByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);

    @Query("SELECT e FROM EmployeeExit e WHERE e.organization.id = :orgId AND e.employee.id = :empId AND e.status NOT IN ('REJECTED', 'SETTLEMENT_COMPLETED', 'PAYMENT_RELEASED')")
    List<EmployeeExit> findActiveExitsForEmployee(@Param("orgId") Long orgId, @Param("empId") Long empId);

    @Query("SELECT e FROM EmployeeExit e WHERE e.organization.id = :orgId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(e.employee.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(e.employee.employeeId) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<EmployeeExit> searchExits(@Param("orgId") Long orgId, @Param("status") String status,
            @Param("search") String search, Pageable pageable);
}
