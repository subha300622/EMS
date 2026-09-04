package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Interview;
import com.example.ems.recruitment.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByOrganizationIdAndId(Long organizationId, Long id);

    List<Interview> findByOrganizationIdAndApplicationId(Long organizationId, Long applicationId);

    List<Interview> findByOrganizationIdAndStatus(Long organizationId, InterviewStatus status);

    @Query("SELECT i FROM Interview i WHERE i.organizationId = :orgId AND i.interviewer.id = :interviewerId " +
           "AND i.scheduledDate = :date AND i.status = 'SCHEDULED' " +
           "AND ((i.startTime < :endTime AND i.endTime > :startTime))")
    List<Interview> findConflictingInterviewerSchedule(
            @Param("orgId") Long orgId,
            @Param("interviewerId") Long interviewerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT i FROM Interview i WHERE i.organizationId = :orgId AND i.application.id = :appId " +
           "AND i.scheduledDate = :date AND i.status = 'SCHEDULED' " +
           "AND ((i.startTime < :endTime AND i.endTime > :startTime))")
    List<Interview> findConflictingCandidateSchedule(
            @Param("orgId") Long orgId,
            @Param("appId") Long appId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
