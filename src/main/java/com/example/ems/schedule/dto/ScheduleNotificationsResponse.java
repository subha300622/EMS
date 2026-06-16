package com.example.ems.schedule.dto;

import java.util.List;

public class ScheduleNotificationsResponse {

    private List<NotificationItem> notifications;

    public ScheduleNotificationsResponse() {}

    public ScheduleNotificationsResponse(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    public static class NotificationItem {
        private Long notificationId;
        private String type;
        private String message;
        private String createdAt;
        private Boolean isRead;

        public NotificationItem() {}

        public NotificationItem(Long notificationId, String type, String message, String createdAt, Boolean isRead) {
            this.notificationId = notificationId;
            this.type = type;
            this.message = message;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }

        // Getters and Setters
        public Long getNotificationId() { return notificationId; }
        public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean read) { isRead = read; }
    }

    public List<NotificationItem> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationItem> notifications) { this.notifications = notifications; }
}
