package com.example.ems.leave.repository;

import com.example.ems.leave.entity.LeaveEncashment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaveEncashmentRepository extends JpaRepository<LeaveEncashment, Long> {
    List<LeaveEncashment> findByEmployeeId(Long employeeId);
    List<LeaveEncashment> findByOrganizationId(Long organizationId);
    List<LeaveEncashment> findByEmployeeIdAndStatus(Long employeeId, String status);

    @Query("SELECT e FROM LeaveEncashment e WHERE e.employee.id = :employeeId " +
           "AND e.status = 'APPROVED' " +
           "AND e.requestedAt >= :startDateTime AND e.requestedAt <= :endDateTime")
    List<LeaveEncashment> findApprovedEncashmentsInPeriod(
            @Param("employeeId") Long employeeId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
