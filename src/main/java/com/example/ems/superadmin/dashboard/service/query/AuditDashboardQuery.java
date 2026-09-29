package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditDashboardQuery {

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public record UserCounts(long totalUsers, long activeUsers) {}

    public UserCounts queryUserCounts() {
        Long total = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();

        Long active = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE UPPER(u.status) = 'ACTIVE'", Long.class)
                .getSingleResult();

        return new UserCounts(
                total != null ? total : 0,
                active != null ? active : 0
        );
    }

    public List<RecentUserDto> queryRecentUsers(int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(
                "SELECT u.id, u.fullName, u.workEmail, " +
                "COALESCE(u.organizationName, o.name), u.status, u.createdAt " +
                "FROM User u " +
                "LEFT JOIN u.organization o " +
                "ORDER BY u.createdAt DESC")
                .setMaxResults(limit)
                .getResultList();

        List<RecentUserDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long userId = (Long) r[0];
            String name = (String) r[1];
            String email = (String) r[2];
            String orgName = (String) r[3];
            String status = (String) r[4];
            Instant createdAt = (Instant) r[5];

            result.add(new RecentUserDto(
                    userId,
                    name != null ? name : "User",
                    email,
                    orgName != null ? orgName : "Platform",
                    status != null ? status : "ACTIVE",
                    createdAt != null ? createdAt.toString() : Instant.now().toString()
            ));
        }
        return result;
    }

    public List<RecentActivityDto> queryRecentActivities(int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(
                "SELECT a.id, a.action, COALESCE(a.entityType, a.module), " +
                "COALESCE(a.recordId, a.entityId), a.userId, a.companyId, a.status, a.createdAt " +
                "FROM AuditLog a " +
                "ORDER BY a.createdAt DESC")
                .setMaxResults(limit)
                .getResultList();

        List<RecentActivityDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long id = (Long) r[0];
            String action = (String) r[1];
            String resource = (String) r[2];
            String resourceId = (String) r[3];
            String actorUserId = (String) r[4];
            Long companyId = (Long) r[5];
            String status = (String) r[6];
            LocalDateTime createdAt = (LocalDateTime) r[7];

            String timestampStr = createdAt != null
                    ? createdAt.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER)
                    : Instant.now().toString();

            result.add(new RecentActivityDto(
                    id,
                    action != null ? action : "UNKNOWN_ACTION",
                    resource != null ? resource : "SYSTEM",
                    resourceId != null ? resourceId : "",
                    actorUserId != null ? actorUserId : "",
                    companyId != null ? companyId.toString() : "",
                    status != null ? status : "SUCCESS",
                    timestampStr
            ));
        }
        return result;
    }
}
