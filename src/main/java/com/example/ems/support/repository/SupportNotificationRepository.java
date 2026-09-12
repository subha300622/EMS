package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportNotificationRepository extends JpaRepository<SupportNotification, Long> {
    List<SupportNotification> findByReceiverUserIdOrderByCreatedAtDesc(Long receiverUserId);
}
