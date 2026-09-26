package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.leave.entity.LeaveStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.LeaveStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LeaveDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public LeaveStats queryLeave(LocalDate from, LocalDate to) {
        Long pending = entityManager.createQuery(
                "SELECT COUNT(l) FROM Leave l WHERE UPPER(l.status) = :status", Long.class)
                .setParameter("status", LeaveStatus.PENDING)
                .getSingleResult();

        Long approved = entityManager.createQuery(
                "SELECT COUNT(l) FROM Leave l " +
                "WHERE UPPER(l.status) = :status " +
                "AND l.startDate >= :from AND l.startDate <= :to", Long.class)
                .setParameter("status", LeaveStatus.APPROVED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        Long rejected = entityManager.createQuery(
                "SELECT COUNT(l) FROM Leave l " +
                "WHERE UPPER(l.status) = :status " +
                "AND l.startDate >= :from AND l.startDate <= :to", Long.class)
                .setParameter("status", LeaveStatus.REJECTED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        Long cancelled = entityManager.createQuery(
                "SELECT COUNT(l) FROM Leave l " +
                "WHERE UPPER(l.status) = :status " +
                "AND l.startDate >= :from AND l.startDate <= :to", Long.class)
                .setParameter("status", LeaveStatus.CANCELLED)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new LeaveStats(
                pending != null ? pending : 0,
                approved != null ? approved : 0,
                rejected != null ? rejected : 0,
                cancelled != null ? cancelled : 0
        );
    }
}
