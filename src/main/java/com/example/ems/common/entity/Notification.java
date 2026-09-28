package com.example.ems.common.entity;

import com.example.ems.auth.entity.User;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_org_read", columnList = "user_id, organization_id, is_read"),
        @Index(name = "idx_notifications_org_id", columnList = "organization_id"),
        @Index(name = "idx_notifications_category", columnList = "category"),
        @Index(name = "idx_notifications_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_notifications_dismissed", columnList = "dismissed")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subscriptions", "tenant", "address", "settings", "activeSubscription"})
    private Organization organization;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false, length = 50)
    @ColumnDefault("'SYSTEM'")
    private String type = "SYSTEM";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @ColumnDefault("'SYSTEM'")
    private NotificationCategory category = NotificationCategory.SYSTEM;

    @Column(nullable = false, length = 50)
    @ColumnDefault("'MEDIUM'")
    private String priority = "MEDIUM";

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "action_required", nullable = false)
    @ColumnDefault("false")
    private boolean actionRequired = false;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean dismissed = false;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "idempotency_key", length = 150)
    private String idempotencyKey;

    public Notification() {}

    public Notification(Long id, User user, String title, String message, String type, String priority, boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.isRead = isRead;
        this.createdAt = createdAt;
        if (user != null && user.getOrganizationId() != null) {
            this.organizationId = user.getOrganizationId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && this.organizationId == null && user.getOrganizationId() != null) {
            this.organizationId = user.getOrganizationId();
        }
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            this.organizationId = organization.getId();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public NotificationType getNotificationType() {
        if (type == null) return null;
        try {
            return NotificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setNotificationType(NotificationType notificationType) {
        if (notificationType != null) {
            this.type = notificationType.name();
        }
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category != null ? category : NotificationCategory.SYSTEM;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public NotificationPriority getNotificationPriority() {
        if (priority == null) return NotificationPriority.MEDIUM;
        try {
            return NotificationPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NotificationPriority.MEDIUM;
        }
    }

    public void setNotificationPriority(NotificationPriority priority) {
        if (priority != null) {
            this.priority = priority.name();
        }
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        if (read && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        } else if (!read) {
            this.readAt = null;
        }
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
        if (dismissed && this.dismissedAt == null) {
            this.dismissedAt = LocalDateTime.now();
        } else if (!dismissed) {
            this.dismissedAt = null;
        }
    }

    public LocalDateTime getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(LocalDateTime dismissedAt) {
        this.dismissedAt = dismissedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
