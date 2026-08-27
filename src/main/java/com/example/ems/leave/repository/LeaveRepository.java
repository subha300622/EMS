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

    @Query("SELECT l FROM Leave l WHERE (:orgId IS NULL OR l.organization.id = :orgId) " +
           "AND (:employeeId IS NULL OR l.employee.id = :employeeId) " +
           "AND (:leaveTypeId IS NULL OR l.leaveType.id = :leaveTypeId) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:fromDate IS NULL OR l.endDate >= :fromDate) " +
           "AND (:toDate IS NULL OR l.startDate <= :toDate) " +
           "AND (:departmentId IS NULL OR l.employee.department.id = :departmentId)")
    List<Leave> findFilteredLeaves(
            @Param("orgId") Long orgId,
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId,
            @Param("status") String status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("departmentId") Long departmentId
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
}
