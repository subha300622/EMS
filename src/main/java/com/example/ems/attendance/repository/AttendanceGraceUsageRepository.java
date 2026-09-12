package com.example.ems.attendance.repository;

import com.example.ems.attendance.entity.AttendanceGraceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceGraceUsageRepository extends JpaRepository<AttendanceGraceUsage, Long> {

    @Query("SELECT COUNT(g) FROM AttendanceGraceUsage g " +
           "WHERE g.organization.id = :orgId " +
           "AND g.employee.id = :employeeId " +
           "AND g.graceType = :graceType " +
           "AND g.withinGrace = true " +
           "AND g.attendanceDate BETWEEN :startDate AND :endDate")
    long countGraceUsagesInPeriod(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("graceType") String graceType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT g FROM AttendanceGraceUsage g " +
           "WHERE g.organization.id = :orgId " +
           "AND g.employee.id = :employeeId " +
           "AND g.attendanceDate = :attendanceDate " +
           "AND g.graceType = :graceType")
    Optional<AttendanceGraceUsage> findByOrgEmpDateAndType(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("graceType") String graceType
    );

    List<AttendanceGraceUsage> findByOrganizationIdAndEmployeeIdAndAttendanceDate(Long organizationId, Long employeeId, LocalDate attendanceDate);

    List<AttendanceGraceUsage> findByOrganizationIdAndEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
            Long organizationId, Long employeeId, LocalDate startDate, LocalDate endDate
    );
}
