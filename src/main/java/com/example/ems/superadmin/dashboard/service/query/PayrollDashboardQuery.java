package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.payroll.entity.PayrollRunStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.PayrollStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PayrollDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<PayrollRunStatus> PENDING_STATUSES = List.of(
            PayrollRunStatus.DRAFT,
            PayrollRunStatus.CALCULATING,
            PayrollRunStatus.PROCESSING,
            PayrollRunStatus.CALCULATED,
            PayrollRunStatus.PENDING_APPROVAL
    );

    private static final List<PayrollRunStatus> PROCESSED_STATUSES = List.of(
            PayrollRunStatus.APPROVED,
            PayrollRunStatus.LOCKED,
            PayrollRunStatus.FINALIZED,
            PayrollRunStatus.PAYMENT_PROCESSING,
            PayrollRunStatus.PAID
    );

    public PayrollStats queryPayroll() {
        Long pending = entityManager.createQuery(
                "SELECT COUNT(r) FROM PayrollRun r WHERE r.status IN :statuses", Long.class)
                .setParameter("statuses", PENDING_STATUSES)
                .getSingleResult();

        Long processed = entityManager.createQuery(
                "SELECT COUNT(r) FROM PayrollRun r WHERE r.status IN :statuses", Long.class)
                .setParameter("statuses", PROCESSED_STATUSES)
                .getSingleResult();

        Long failed = entityManager.createQuery(
                "SELECT COUNT(r) FROM PayrollRun r WHERE r.status = :status", Long.class)
                .setParameter("status", PayrollRunStatus.FAILED)
                .getSingleResult();

        BigDecimal totalPayroll = entityManager.createQuery(
                "SELECT COALESCE(SUM(r.totalNet), 0) FROM PayrollRun r WHERE r.status IN :statuses", BigDecimal.class)
                .setParameter("statuses", PROCESSED_STATUSES)
                .getSingleResult();

        return new PayrollStats(
                pending != null ? pending : 0,
                processed != null ? processed : 0,
                failed != null ? failed : 0,
                totalPayroll != null ? totalPayroll : BigDecimal.ZERO,
                "INR"
        );
    }
}
