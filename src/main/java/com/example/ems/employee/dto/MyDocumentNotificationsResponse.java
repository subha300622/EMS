package com.example.ems.employee.dto;

import java.time.LocalDate;
import java.util.List;

public class MyDocumentNotificationsResponse {

    private List<NotificationItem> notifications;

    public MyDocumentNotificationsResponse() {}

    public MyDocumentNotificationsResponse(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    public List<NotificationItem> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    public static class NotificationItem {
        private Long documentId;
        private String documentName;
        private String status;
        private int daysRemaining;
        private LocalDate expiryDate;

        public NotificationItem() {}

        public NotificationItem(Long documentId, String documentName, String status, int daysRemaining, LocalDate expiryDate) {
            this.documentId = documentId;
            this.documentName = documentName;
            this.status = status;
            this.daysRemaining = daysRemaining;
            this.expiryDate = expiryDate;
        }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getDocumentName() { return documentName; }
        public void setDocumentName(String documentName) { this.documentName = documentName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    }
}
