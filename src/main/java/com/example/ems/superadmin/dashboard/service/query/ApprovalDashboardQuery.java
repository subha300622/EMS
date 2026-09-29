package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.approval.entity.ApprovalStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class ApprovalDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public long queryPendingApprovals() {
        Long count = entityManager.createQuery(
                "SELECT COUNT(w) FROM ApprovalWorkflowInstance w WHERE w.status = :status", Long.class)
                .setParameter("status", ApprovalStatus.PENDING)
                .getSingleResult();
        return count != null ? count : 0;
    }
}
