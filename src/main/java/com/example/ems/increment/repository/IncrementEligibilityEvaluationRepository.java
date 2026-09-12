package com.example.ems.increment.repository;

import com.example.ems.increment.entity.IncrementEligibilityEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncrementEligibilityEvaluationRepository extends JpaRepository<IncrementEligibilityEvaluation, Long> {

    @Query("SELECT e FROM IncrementEligibilityEvaluation e WHERE e.cycle.id = :cycleId AND e.employee.id = :employeeId ORDER BY e.id DESC")
    List<IncrementEligibilityEvaluation> findByCycleIdAndEmployeeId(@Param("cycleId") Long cycleId, @Param("employeeId") Long employeeId);

    @Query("SELECT e FROM IncrementEligibilityEvaluation e WHERE e.cycle.id = :cycleId AND e.employee.id = :employeeId ORDER BY e.id DESC")
    Optional<IncrementEligibilityEvaluation> findFirstByCycleIdAndEmployeeId(@Param("cycleId") Long cycleId, @Param("employeeId") Long employeeId);

    @Query("SELECT e FROM IncrementEligibilityEvaluation e WHERE e.cycle.id = :cycleId")
    List<IncrementEligibilityEvaluation> findByCycleId(@Param("cycleId") Long cycleId);
}
