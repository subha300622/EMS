package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public record EmployeeCounts(
            long total,
            long active,
            long inactive,
            long newEmployees,
            long exitedEmployees,
            double growthPercentage
    ) {}

    public EmployeeCounts queryCounts(LocalDate from, LocalDate to, LocalDate prevFrom, LocalDate prevTo) {
        Long total = entityManager.createQuery(
                "SELECT COUNT(e) FROM Employee e", Long.class)
                .getSingleResult();

        Long active = entityManager.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE UPPER(e.status) = 'ACTIVE'", Long.class)
                .getSingleResult();

        long currentTotal = total != null ? total : 0;
        long currentActive = active != null ? active : 0;
        long currentInactive = Math.max(0, currentTotal - currentActive);

        Long newCount = entityManager.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.joiningDate >= :from AND e.joiningDate <= :to", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        Long prevNewCount = entityManager.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.joiningDate >= :prevFrom AND e.joiningDate <= :prevTo", Long.class)
                .setParameter("prevFrom", prevFrom)
                .setParameter("prevTo", prevTo)
                .getSingleResult();

        long currentNew = newCount != null ? newCount : 0;
        long prevNew = prevNewCount != null ? prevNewCount : 0;

        // Exited employees source of truth: EmployeeExit repository entity
        Long exitedCount = entityManager.createQuery(
                "SELECT COUNT(x) FROM EmployeeExit x " +
                "WHERE x.lastWorkingDate >= :from AND x.lastWorkingDate <= :to " +
                "AND x.status NOT IN ('REJECTED', 'CANCELLED')", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        long currentExited = exitedCount != null ? exitedCount : 0;

        double growth = 0.0;
        if (prevNew > 0) {
            growth = Math.round(((double) (currentNew - prevNew) / prevNew) * 10000.0) / 100.0;
        } else if (currentNew > 0) {
            growth = 100.0;
        }

        return new EmployeeCounts(
                currentTotal,
                currentActive,
                currentInactive,
                currentNew,
                currentExited,
                growth
        );
    }

    public List<ChartPointDto> queryGrowthChart() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT TO_CHAR(DATE_TRUNC('month', joining_date), 'YYYY-MM') AS period, " +
                    "COUNT(*) AS cnt " +
                    "FROM employees " +
                    "WHERE joining_date IS NOT NULL " +
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
