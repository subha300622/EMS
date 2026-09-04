package com.example.ems.schedule.repository;

import com.example.ems.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

    Optional<Schedule> findByScheduleIdAndOrganizationId(String scheduleId, Long organizationId);

    @Query("SELECT s FROM Schedule s WHERE s.organization.id = :organizationId " +
           "AND (s.employee.id = :employeeId OR s.employee.employeeId = :employeeIdStr)")
    List<Schedule> findByEmployeeAndOrganization(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("employeeIdStr") String employeeIdStr
    );

    @Query("SELECT s FROM Schedule s WHERE s.organization.id = :organizationId " +
           "AND s.employee.team IS NOT NULL AND s.employee.team.id = :teamId")
    List<Schedule> findByTeamAndOrganization(
            @Param("organizationId") Long organizationId,
            @Param("teamId") Long teamId
    );

    @Query("SELECT s FROM Schedule s WHERE s.organization.id = :organizationId " +
           "AND s.employee.department = :departmentName")
    List<Schedule> findByDepartmentNameAndOrganization(
            @Param("organizationId") Long organizationId,
            @Param("departmentName") String departmentName
    );

    @Query("SELECT COUNT(s) > 0 FROM Schedule s " +
           "WHERE s.organization.id = :organizationId " +
           "AND s.employee.id = :employeeId " +
           "AND s.date = :date " +
           "AND s.status <> com.example.ems.schedule.entity.ScheduleStatus.CANCELLED " +
           "AND s.startTime < :endTime " +
           "AND s.endTime > :startTime")
    boolean existsOverlappingForCreate(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(s) > 0 FROM Schedule s " +
           "WHERE s.organization.id = :organizationId " +
           "AND s.employee.id = :employeeId " +
           "AND s.date = :date " +
           "AND s.id <> :excludeId " +
           "AND s.status <> com.example.ems.schedule.entity.ScheduleStatus.CANCELLED " +
           "AND s.startTime < :endTime " +
           "AND s.endTime > :startTime")
    boolean existsOverlappingForUpdate(
            @Param("organizationId") Long organizationId,
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("excludeId") Long excludeId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
