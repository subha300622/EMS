package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.organization.entity.SubscriptionStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubscriptionDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public record SubscriptionCounts(
            long total,
            long active,
            long trial,
            long expired,
            long cancelled,
            long expiringSoon
    ) {}

    public SubscriptionCounts queryCounts() {
        Long total = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s", Long.class)
                .getSingleResult();

        Long active = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = :status", Long.class)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getSingleResult();

        Long trial = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = :status", Long.class)
                .setParameter("status", SubscriptionStatus.TRIAL)
                .getSingleResult();

        Long expired = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = :status", Long.class)
                .setParameter("status", SubscriptionStatus.EXPIRED)
                .getSingleResult();

        Long cancelled = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.status = :status", Long.class)
                .setParameter("status", SubscriptionStatus.CANCELLED)
                .getSingleResult();

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        Long expiringSoon = entityManager.createQuery(
                "SELECT COUNT(s) FROM Subscription s WHERE s.expiryDate >= :today AND s.expiryDate <= :in30Days AND s.status = :status", Long.class)
                .setParameter("today", today)
                .setParameter("in30Days", in30Days)
                .setParameter("status", SubscriptionStatus.ACTIVE)
                .getSingleResult();

        return new SubscriptionCounts(
                total != null ? total : 0,
                active != null ? active : 0,
                trial != null ? trial : 0,
                expired != null ? expired : 0,
                cancelled != null ? cancelled : 0,
                expiringSoon != null ? expiringSoon : 0
        );
    }

    public List<ChartPointDto> querySubscriptionTrend() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT TO_CHAR(DATE_TRUNC('month', start_date), 'YYYY-MM') AS period, " +
                    "COUNT(*) AS cnt " +
                    "FROM subscriptions " +
                    "WHERE start_date IS NOT NULL " +
                    "GROUP BY period " +
                    "ORDER BY period ASC " +
                    "LIMIT 12")
                    .getResultList();

            List<ChartPointDto> points = new ArrayList<>();
            for (Object[] row : rows) {
                String period = (String) row[0];
                long count = ((Number) row[1]).longValue();
                points.add(new ChartPointDto(period, count));
            }
            return points;
        } catch (Exception e) {
            return List.of();
        }
    }
}
