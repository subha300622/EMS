package com.example.ems.common.repository;

import com.example.ems.common.entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
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

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "INSERT INTO public.notifications (user_id, title, message, type, priority, is_read, created_at, idempotency_key) " +
                   "VALUES (:userId, :title, :message, :type, :priority, :isRead, :createdAt, :idempotencyKey) " +
                   "ON CONFLICT (idempotency_key) DO NOTHING", nativeQuery = true)
    int insertNotificationIfNotExists(@org.springframework.data.repository.query.Param("userId") Long userId,
                                     @org.springframework.data.repository.query.Param("title") String title,
                                     @org.springframework.data.repository.query.Param("message") String message,
                                     @org.springframework.data.repository.query.Param("type") String type,
                                     @org.springframework.data.repository.query.Param("priority") String priority,
                                     @org.springframework.data.repository.query.Param("isRead") boolean isRead,
                                     @org.springframework.data.repository.query.Param("createdAt") java.time.LocalDateTime createdAt,
                                     @org.springframework.data.repository.query.Param("idempotencyKey") String idempotencyKey);
}
