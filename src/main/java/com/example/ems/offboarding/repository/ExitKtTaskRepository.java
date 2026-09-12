package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitKtTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExitKtTaskRepository extends JpaRepository<ExitKtTask, Long> {

    @Query("SELECT COUNT(t) FROM ExitKtTask t WHERE t.ktPlan.employee.id = :employeeId AND t.status != 'COMPLETED'")
    long countPendingByOffboardingId(@Param("employeeId") Long employeeId);
}
