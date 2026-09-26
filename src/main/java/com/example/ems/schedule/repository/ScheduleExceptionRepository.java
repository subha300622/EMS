package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    Optional<ScheduleException> findByLeaveRequestIdAndExceptionType(Long leaveRequestId, String exceptionType);

    @Query("SELECT se FROM ScheduleException se WHERE se.employeeId = :employeeId AND se.status = 'ACTIVE' AND se.startDate <= :targetDate AND se.endDate >= :targetDate")
    List<ScheduleException> findActiveExceptionsOnDate(@Param("employeeId") String employeeId, @Param("targetDate") LocalDate targetDate);

    @Query("SELECT se FROM ScheduleException se WHERE se.employeeId = :employeeId AND se.status = 'ACTIVE' AND se.startDate <= :endDate AND se.endDate >= :startDate")
    List<ScheduleException> findActiveExceptionsInDateRange(
            @Param("employeeId") String employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
