package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class RevenueDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public record RevenueStatsResult(
            BigDecimal currentPeriod,
            BigDecimal previousPeriod,
            double growthPercentage,
            String currency,
            long transactions
    ) {}

    public RevenueStatsResult queryRevenue(LocalDate from, LocalDate to, LocalDate prevFrom, LocalDate prevTo) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

        Instant prevFromInstant = prevFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant prevToInstant = prevTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

        BigDecimal current = entityManager.createQuery(
                "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                "WHERE p.status = 'SUCCESS' " +
                "AND COALESCE(p.paidAt, p.createdAt) >= :from " +
                "AND COALESCE(p.paidAt, p.createdAt) <= :to", BigDecimal.class)
                .setParameter("from", fromInstant)
                .setParameter("to", toInstant)
                .getSingleResult();

        BigDecimal previous = entityManager.createQuery(
                "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                "WHERE p.status = 'SUCCESS' " +
                "AND COALESCE(p.paidAt, p.createdAt) >= :prevFrom " +
                "AND COALESCE(p.paidAt, p.createdAt) <= :prevTo", BigDecimal.class)
                .setParameter("prevFrom", prevFromInstant)
                .setParameter("prevTo", prevToInstant)
                .getSingleResult();

        Long txCount = entityManager.createQuery(
                "SELECT COUNT(p) FROM Payment p " +
                "WHERE p.status = 'SUCCESS' " +
                "AND COALESCE(p.paidAt, p.createdAt) >= :from " +
                "AND COALESCE(p.paidAt, p.createdAt) <= :to", Long.class)
                .setParameter("from", fromInstant)
                .setParameter("to", toInstant)
                .getSingleResult();

        BigDecimal currentAmount = current != null ? current : BigDecimal.ZERO;
        BigDecimal previousAmount = previous != null ? previous : BigDecimal.ZERO;

        double growth = 0.0;
        if (previousAmount.compareTo(BigDecimal.ZERO) > 0) {
            growth = currentAmount.subtract(previousAmount)
                    .divide(previousAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (currentAmount.compareTo(BigDecimal.ZERO) > 0) {
            growth = 100.0;
        }

        return new RevenueStatsResult(
                currentAmount,
                previousAmount,
                growth,
                "INR",
                txCount != null ? txCount : 0
        );
    }

    public List<RevenueChartDataPoint> queryRevenueTrend() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT TO_CHAR(DATE_TRUNC('month', COALESCE(paid_at, created_at)), 'YYYY-MM') AS period, " +
                    "COALESCE(SUM(amount), 0) AS rev " +
                    "FROM payments " +
                    "WHERE status = 'SUCCESS' " +
                    "GROUP BY period " +
                    "ORDER BY period ASC " +
                    "LIMIT 12")
                    .getResultList();

            List<RevenueChartDataPoint> points = new ArrayList<>();
            for (Object[] row : rows) {
                String period = (String) row[0];
                BigDecimal rev = row[1] instanceof BigDecimal b ? b : BigDecimal.valueOf(((Number) row[1]).doubleValue());
                points.add(new RevenueChartDataPoint(period, rev));
            }
            return points;
        } catch (Exception e) {
            return List.of();
        }
    }
}
