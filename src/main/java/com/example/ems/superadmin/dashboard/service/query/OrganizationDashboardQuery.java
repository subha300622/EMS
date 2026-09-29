package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrganizationDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public record OrganizationCounts(
            long total,
            long active,
            long inactive,
            long newCount,
            long activated,
            long suspended,
            double growthPercentage
    ) {}

    public OrganizationCounts queryCounts(LocalDate from, LocalDate to, LocalDate prevFrom, LocalDate prevTo) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

        Instant prevFromInstant = prevFrom.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant prevToInstant = prevTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusNanos(1);

        Long total = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.isDeleted = false", Long.class)
                .getSingleResult();

        Long active = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.isDeleted = false AND o.status = :status", Long.class)
                .setParameter("status", OrganizationStatus.ACTIVE)
                .getSingleResult();

        Long suspended = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.isDeleted = false AND o.status = :status", Long.class)
                .setParameter("status", OrganizationStatus.SUSPENDED)
                .getSingleResult();

        long inactive = Math.max(0, (total != null ? total : 0) - (active != null ? active : 0));

        Long newCount = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.isDeleted = false AND o.createdAt >= :from AND o.createdAt <= :to", Long.class)
                .setParameter("from", fromInstant)
                .setParameter("to", toInstant)
                .getSingleResult();

        Long prevNewCount = entityManager.createQuery(
                "SELECT COUNT(o) FROM Organization o WHERE o.isDeleted = false AND o.createdAt >= :prevFrom AND o.createdAt <= :prevTo", Long.class)
                .setParameter("prevFrom", prevFromInstant)
                .setParameter("prevTo", prevToInstant)
                .getSingleResult();

        long currentNew = newCount != null ? newCount : 0;
        long previousNew = prevNewCount != null ? prevNewCount : 0;

        double growth = 0.0;
        if (previousNew > 0) {
            growth = Math.round(((double) (currentNew - previousNew) / previousNew) * 10000.0) / 100.0;
        } else if (currentNew > 0) {
            growth = 100.0;
        }

        long activated = Math.min(currentNew, active != null ? active : 0);

        return new OrganizationCounts(
                total != null ? total : 0,
                active != null ? active : 0,
                inactive,
                currentNew,
                activated,
                suspended != null ? suspended : 0,
                growth
        );
    }

    public List<RecentOrganizationDto> queryRecentOrganizations(int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(
                "SELECT o.id, o.name, o.status, " +
                "(SELECT COUNT(e) FROM Employee e WHERE e.organization.id = o.id), " +
                "o.createdAt " +
                "FROM Organization o WHERE o.isDeleted = false " +
                "ORDER BY o.createdAt DESC")
                .setMaxResults(limit)
                .getResultList();

        List<RecentOrganizationDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long orgId = (Long) r[0];
            String name = (String) r[1];
            OrganizationStatus status = (OrganizationStatus) r[2];
            Long empCount = (Long) r[3];
            Instant createdAt = (Instant) r[4];

            result.add(new RecentOrganizationDto(
                    orgId,
                    name,
                    status != null ? status.name() : "ACTIVE",
                    empCount != null ? empCount : 0,
                    createdAt != null ? createdAt.toString() : Instant.now().toString()
            ));
        }
        return result;
    }

    public List<ChartPointDto> queryGrowthChart() {
        // Native aggregation with DATE_TRUNC for PostgreSQL
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS period, " +
                    "COUNT(*) AS cnt " +
                    "FROM organizations " +
                    "WHERE is_deleted = false " +
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
