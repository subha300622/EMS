package com.example.ems.common.event;

/**
 * Integration event payload for notification creation.
 * This is the stable contract published to Kafka.
 */
public class NotificationCreatedEvent {

    private Long userId;
    private String title;
    private String message;
    private String type;
    private String priority;

    public NotificationCreatedEvent() {
    }

    public NotificationCreatedEvent(Long userId, String title, String message,
                                     String type, String priority) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
