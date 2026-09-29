package com.example.ems.schedule.swap.repository;

import com.example.ems.schedule.swap.entity.ScheduleSwapRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleSwapRequestRepository extends JpaRepository<ScheduleSwapRequest, Long>, JpaSpecificationExecutor<ScheduleSwapRequest> {

    Optional<ScheduleSwapRequest> findByRequestIdAndOrganizationId(String requestId, Long organizationId);

    Optional<ScheduleSwapRequest> findByRequestId(String requestId);

    Optional<ScheduleSwapRequest> findByWorkflowInstanceId(String workflowInstanceId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ScheduleSwapRequest r " +
           "WHERE r.organization.id = :orgId " +
           "AND r.status IN ('PENDING_APPROVAL', 'IN_PROGRESS', 'APPROVED') " +
           "AND ((r.sourceSchedule.id = :sourceId AND r.targetSchedule.id = :targetId) " +
           "  OR (r.sourceSchedule.id = :targetId AND r.targetSchedule.id = :sourceId))")
    boolean existsActiveSwapForSchedules(
            @Param("orgId") Long orgId,
            @Param("sourceId") Long sourceId,
            @Param("targetId") Long targetId
    );

    Page<ScheduleSwapRequest> findByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT r FROM ScheduleSwapRequest r WHERE r.organization.id = :orgId " +
           "AND (r.sourceEmployee.id = :employeeId OR r.targetEmployee.id = :employeeId OR r.createdBy.id = :employeeId)")
    Page<ScheduleSwapRequest> findMySwapRequests(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            Pageable pageable
    );
}
