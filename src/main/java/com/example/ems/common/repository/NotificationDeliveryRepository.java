package com.example.ems.common.repository;

import com.example.ems.common.entity.DeliveryChannel;
import com.example.ems.common.entity.DeliveryStatus;
import com.example.ems.common.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
    List<NotificationDelivery> findByNotificationId(Long notificationId);
    List<NotificationDelivery> findByRecipientUserId(Long recipientUserId);
    List<NotificationDelivery> findByChannelAndStatus(DeliveryChannel channel, DeliveryStatus status);
}
