package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.FnfSettlement;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository("exitFnfSettlementRepository")
public interface ExitFnfSettlementRepository extends JpaRepository<FnfSettlement, Long>, JpaSpecificationExecutor<FnfSettlement> {

    Optional<FnfSettlement> findByIdAndOrganizationId(Long id, Long organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ExitFnfSettlement s WHERE s.id = :id AND s.organization.id = :orgId")
    Optional<FnfSettlement> findWithLockByIdAndOrganizationId(@Param("id") Long id, @Param("orgId") Long orgId);

    Optional<FnfSettlement> findByOrganizationIdAndPaymentReference(Long organizationId, String paymentReference);

    Optional<FnfSettlement> findByOrganizationIdAndIdempotencyKey(Long organizationId, String idempotencyKey);

    Optional<FnfSettlement> findByExitIdAndOrganizationId(Long exitId, Long organizationId);

    Optional<FnfSettlement> findByExitId(Long exitId);

    Page<FnfSettlement> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<FnfSettlement> findByOrganizationIdAndStatus(Long organizationId, String status, Pageable pageable);

    List<FnfSettlement> findByOrganizationIdAndStatus(Long organizationId, String status);

    @Query("SELECT s FROM ExitFnfSettlement s WHERE s.organization.id = :orgId AND s.status != 'PAYMENT_RELEASED' AND s.status != 'REJECTED' AND s.status != 'CANCELLED'")
    Page<FnfSettlement> findPendingSettlements(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT s FROM ExitFnfSettlement s WHERE s.organization.id = :orgId AND (s.status = 'PAYMENT_RELEASED' OR s.status = 'FINALIZED')")
    Page<FnfSettlement> findPaidSettlements(@Param("orgId") Long orgId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.paidAmount), 0) FROM ExitFnfSettlement s WHERE s.organization.id = :orgId AND (s.status = 'PAYMENT_RELEASED' OR s.status = 'FINALIZED')")
    BigDecimal sumTotalPaidSettlements(@Param("orgId") Long orgId);

    @Query("SELECT COALESCE(SUM(s.netSettlement), 0) FROM ExitFnfSettlement s WHERE s.organization.id = :orgId AND s.status != 'PAYMENT_RELEASED' AND s.status != 'FINALIZED' AND s.status != 'REJECTED' AND s.status != 'CANCELLED'")
    BigDecimal sumTotalPendingSettlements(@Param("orgId") Long orgId);

    @Query("SELECT s FROM ExitFnfSettlement s WHERE s.exit.employee.id = :employeeId AND s.organization.id = :orgId ORDER BY s.createdAt DESC")
    List<FnfSettlement> findByEmployeeIdAndOrganizationId(@Param("employeeId") Long employeeId, @Param("orgId") Long orgId);

    default List<FnfSettlement> findByEmployeeId(Long employeeId, Long orgId) {
        return findByEmployeeIdAndOrganizationId(employeeId, orgId);
    }

    @Query("SELECT s FROM ExitFnfSettlement s WHERE s.exit.employee.email = :email AND s.organization.id = :orgId ORDER BY s.createdAt DESC")
    Optional<FnfSettlement> findByEmployeeEmailAndOrganizationId(@Param("email") String email, @Param("orgId") Long orgId);

    Page<FnfSettlement> findByOrganizationIdAndStatusIn(Long organizationId, List<String> statuses, Pageable pageable);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    long countByOrganizationId(Long organizationId);
}
