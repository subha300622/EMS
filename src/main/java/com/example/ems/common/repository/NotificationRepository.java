package com.example.ems.common.repository;

import com.example.ems.common.entity.Notification;
import com.example.ems.common.entity.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    // ── Existing backward-compatible queries ───────────────────────────────────
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable);
    Page<Notification> findByUserIdAndType(Long userId, String type, Pageable pageable);
    Page<Notification> findByUserIdAndTypeAndIsRead(Long userId, String type, boolean isRead, Pageable pageable);

    long countByUserId(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
    long countByUserIdAndType(Long userId, String type);

    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query(value = "INSERT INTO public.notifications (user_id, title, message, type, priority, is_read, created_at, idempotency_key) " +
                   "VALUES (:userId, :title, :message, :type, :priority, :isRead, :createdAt, :idempotencyKey) " +
                   "ON CONFLICT (idempotency_key) DO NOTHING", nativeQuery = true)
    int insertNotificationIfNotExists(@Param("userId") Long userId,
                                      @Param("title") String title,
                                      @Param("message") String message,
                                      @Param("type") String type,
                                      @Param("priority") String priority,
                                      @Param("isRead") boolean isRead,
                                      @Param("createdAt") java.time.LocalDateTime createdAt,
                                      @Param("idempotencyKey") String idempotencyKey);

    // ── Multi-tenant and permission-aware scoped queries ────────────────────────
    Optional<Notification> findByIdAndUserIdAndOrganizationId(Long id, Long userId, Long organizationId);

    Page<Notification> findByUserIdAndOrganizationIdAndDismissedFalseOrderByCreatedAtDesc(
            Long userId, Long organizationId, Pageable pageable);

    Page<Notification> findByUserIdAndOrganizationIdAndIsReadAndDismissedFalseOrderByCreatedAtDesc(
            Long userId, Long organizationId, boolean isRead, Pageable pageable);

    Page<Notification> findByUserIdAndOrganizationIdAndCategoryAndDismissedFalseOrderByCreatedAtDesc(
            Long userId, Long organizationId, NotificationCategory category, Pageable pageable);

    long countByUserIdAndOrganizationIdAndIsReadFalseAndDismissedFalse(Long userId, Long organizationId);

    long countByUserIdAndOrganizationIdAndActionRequiredTrueAndIsReadFalseAndDismissedFalse(
            Long userId, Long organizationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.user.id = :userId AND n.organizationId = :organizationId AND n.isRead = false AND n.dismissed = false")
    int markAllAsReadForUserAndOrg(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.user.id = :userId AND n.organizationId = :organizationId AND n.category = :category AND n.isRead = false AND n.dismissed = false")
    int markAllAsReadForUserAndOrgByCategory(@Param("userId") Long userId,
                                             @Param("organizationId") Long organizationId,
                                             @Param("category") NotificationCategory category);
}
