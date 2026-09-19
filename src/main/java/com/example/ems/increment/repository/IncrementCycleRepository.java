package com.example.ems.increment.repository;

import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementCycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncrementCycleRepository extends JpaRepository<IncrementCycle, Long> {

    @Query("SELECT c FROM IncrementCycle c WHERE c.organization.id = :orgId ORDER BY c.id DESC")
    List<IncrementCycle> findAllByOrgId(@Param("orgId") Long orgId);

    @Query("SELECT c FROM IncrementCycle c WHERE c.id = :id AND c.organization.id = :orgId")
    Optional<IncrementCycle> findByIdAndOrgId(@Param("id") Long id, @Param("orgId") Long orgId);

    @Query("SELECT c FROM IncrementCycle c WHERE c.organization.id = :orgId AND c.status = :status")
    List<IncrementCycle> findAllByOrgIdAndStatus(@Param("orgId") Long orgId, @Param("status") IncrementCycleStatus status);

    @Query("SELECT c FROM IncrementCycle c WHERE c.organization.id = :orgId " +
           "AND c.status IN ('DRAFT', 'OPEN') " +
           "AND ((c.startDate <= :endDate AND c.endDate >= :startDate))")
    List<IncrementCycle> findOverlappingActiveCycles(
            @Param("orgId") Long orgId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
