package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.AttendanceStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AttendanceDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    public AttendanceStats queryAttendance(LocalDate from, LocalDate to) {
        Long present = entityManager.createQuery(
                "SELECT COUNT(a) FROM Attendance a " +
                "WHERE a.date >= :from AND a.date <= :to " +
                "AND a.status IN :presentStatuses", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("presentStatuses", List.of(
                        AttendanceStatus.PRESENT,
                        AttendanceStatus.WORKING,
                        AttendanceStatus.COMPLETED
                ))
                .getSingleResult();

        Long absent = entityManager.createQuery(
                "SELECT COUNT(a) FROM Attendance a " +
                "WHERE a.date >= :from AND a.date <= :to " +
                "AND a.status = :status", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("status", AttendanceStatus.ABSENT)
                .getSingleResult();

        Long late = entityManager.createQuery(
                "SELECT COUNT(a) FROM Attendance a " +
                "WHERE a.date >= :from AND a.date <= :to " +
                "AND a.status = :status", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("status", AttendanceStatus.LATE)
                .getSingleResult();

        Long onLeave = entityManager.createQuery(
                "SELECT COUNT(a) FROM Attendance a " +
                "WHERE a.date >= :from AND a.date <= :to " +
                "AND a.status = :status", Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("status", AttendanceStatus.LEAVE)
                .getSingleResult();

        long p = present != null ? present : 0;
        long a = absent != null ? absent : 0;
        long l = late != null ? late : 0;
        long ol = onLeave != null ? onLeave : 0;

        // Working-day applicability semantics:
        // Applicable events = present + absent + late (excused approved leaves not penalized in working denominator)
        long applicableAttendanceEvents = p + a + l;
        double percentage = 0.0;
        if (applicableAttendanceEvents > 0) {
            percentage = Math.round(((double) p / applicableAttendanceEvents) * 10000.0) / 100.0;
        }

        return new AttendanceStats(p, a, l, ol, percentage);
    }
}
