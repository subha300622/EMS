package com.example.ems.common.dto.notification;

import com.example.ems.common.entity.NotificationCategory;
import com.example.ems.common.entity.NotificationPriority;
import com.example.ems.common.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCommand {

    private Long organizationId;
    private Long recipientUserId;
    private NotificationCategory category;
    private NotificationType type;
    private String customType;
    private String title;
    private String message;
    private NotificationPriority priority;
    private boolean actionRequired;
    private String actionUrl;
    private String entityType;
    private Long entityId;
    private String metadata;
    private String idempotencyKey;
    private LocalDateTime expiresAt;
}
