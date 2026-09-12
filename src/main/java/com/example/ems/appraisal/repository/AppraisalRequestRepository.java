package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalRequest;
import com.example.ems.appraisal.entity.AppraisalRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AppraisalRequestRepository extends JpaRepository<AppraisalRequest, Long> {
    List<AppraisalRequest> findByOrganizationId(Long organizationId);
    List<AppraisalRequest> findByOrganizationIdAndEmployeeId(Long organizationId, Long employeeId);
    Optional<AppraisalRequest> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<AppraisalRequest> findByApprovalInstanceId(String approvalInstanceId);

    @Query("SELECT COUNT(r) > 0 FROM AppraisalRequest r WHERE r.organization.id = :orgId AND r.employee.id = :empId AND r.status IN :statuses")
    boolean existsActiveRequest(
            @Param("orgId") Long orgId,
            @Param("empId") Long empId,
            @Param("statuses") Set<AppraisalRequestStatus> statuses);

    boolean existsByReasonId(Long reasonId);
}
