package com.example.ems.leave.repository;

import com.example.ems.leave.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long>, JpaSpecificationExecutor<Leave> {

    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(String status);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, String status);
    List<Leave> findByEmployeeIdAndLeaveTypeIdAndStatus(Long employeeId, Long leaveTypeId, String status);
    List<Leave> findByEmployeeIdInAndStatus(List<Long> employeeIds, String status);

    Optional<Leave> findByApprovalWorkflowInstanceId(String approvalWorkflowInstanceId);

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.status IN ('PENDING', 'APPROVED') " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<Leave> findOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT l.* FROM leaves l JOIN employees e ON e.id = l.employee_id WHERE " +
                   "(CAST(:orgId AS BIGINT) IS NULL OR l.organization_id = CAST(:orgId AS BIGINT)) AND " +
                   "(CAST(:employeeId AS BIGINT) IS NULL OR l.employee_id = CAST(:employeeId AS BIGINT)) AND " +
                   "(CAST(:leaveTypeId AS BIGINT) IS NULL OR l.leave_type_id = CAST(:leaveTypeId AS BIGINT)) AND " +
                   "(CAST(:status AS VARCHAR) IS NULL OR l.status = CAST(:status AS VARCHAR)) AND " +
                   "(CAST(:fromDate AS DATE) IS NULL OR l.end_date >= CAST(:fromDate AS DATE)) AND " +
                   "(CAST(:toDate AS DATE) IS NULL OR l.start_date <= CAST(:toDate AS DATE)) AND " +
                   "(CAST(:departmentId AS VARCHAR) IS NULL OR e.department = CAST(:departmentId AS VARCHAR))", nativeQuery = true)
    List<Leave> findFilteredLeaves(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("departmentId") String departmentId
    );

    @Query("SELECT l FROM Leave l WHERE l.approver.id = :managerId " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:employeeId IS NULL OR l.employee.id = :employeeId) " +
           "AND (:fromDate IS NULL OR l.startDate >= :fromDate) " +
           "AND (:toDate IS NULL OR l.endDate <= :toDate)")
    Page<Leave> findManagerLeaveApprovals(
            @Param("managerId") Long managerId,
            @Param("status") String status,
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.approver.id = :managerId AND l.status = 'PENDING'")
    long countPendingForManager(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.approver.id = :managerId AND l.status = 'APPROVED' AND l.approvedAt >= :startOfToday")
    long countApprovedTodayForManager(@Param("managerId") Long managerId, @Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.approver.id = :managerId AND l.status = 'REJECTED' AND l.rejectedAt >= :startOfToday")
    long countRejectedTodayForManager(@Param("managerId") Long managerId, @Param("startOfToday") LocalDateTime startOfToday);

    @Query(value = "SELECT l.* FROM leaves l " +
           "JOIN employees e ON e.id = l.employee_id " +
           "LEFT JOIN my_teams t ON t.id = e.team_id " +
           "WHERE (CAST(:orgId AS BIGINT) IS NULL OR l.organization_id = CAST(:orgId AS BIGINT)) " +
           "AND (CAST(:employeeId AS BIGINT) IS NULL OR l.employee_id = CAST(:employeeId AS BIGINT)) " +
           "AND (CAST(:teamId AS BIGINT) IS NULL OR t.id = CAST(:teamId AS BIGINT)) " +
           "AND (CAST(:department AS VARCHAR) IS NULL OR LOWER(e.department) = CAST(:department AS VARCHAR)) " +
           "AND (CAST(:leaveTypeId AS BIGINT) IS NULL OR l.leave_type_id = CAST(:leaveTypeId AS BIGINT)) " +
           "AND (l.status IN (:statuses)) " +
           "AND (CAST(:startDate AS DATE) IS NULL OR l.end_date >= CAST(:startDate AS DATE)) " +
           "AND (CAST(:endDate AS DATE) IS NULL OR l.start_date <= CAST(:endDate AS DATE)) " +
           "ORDER BY l.start_date ASC", nativeQuery = true)
    List<Leave> findCalendarEvents(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("teamId") Long teamId,
            @Param("department") String department,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("statuses") List<String> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
