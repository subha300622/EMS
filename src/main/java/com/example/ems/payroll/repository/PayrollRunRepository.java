package com.example.ems.payroll.repository;

import com.example.ems.payroll.entity.PayrollRun;
import com.example.ems.payroll.entity.PayrollRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    Optional<PayrollRun> findByIdAndOrganizationId(Long id, Long organizationId);

    List<PayrollRun> findByOrganizationIdOrderByPeriodStartDesc(Long organizationId);

    List<PayrollRun> findByOrganizationIdAndStatusOrderByPeriodStartDesc(Long organizationId, PayrollRunStatus status);

    boolean existsByOrganizationIdAndPeriodStartAndPeriodEnd(Long organizationId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT r FROM PayrollRun r WHERE r.organizationId = :orgId AND " +
           "((r.periodStart <= :end AND r.periodEnd >= :start))")
    List<PayrollRun> findOverlappingRuns(
            @Param("orgId") Long organizationId,
            @Param("start") LocalDate periodStart,
            @Param("end") LocalDate periodEnd
    );
}
