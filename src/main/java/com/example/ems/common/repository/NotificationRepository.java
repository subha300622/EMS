package com.example.ems.common.repository;

import com.example.ems.common.entity.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

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
}
