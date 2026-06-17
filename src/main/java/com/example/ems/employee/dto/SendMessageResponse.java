package com.example.ems.employee.dto;

public class SendMessageResponse {
    private Long messageId;
    private String status;
    private String sentAt;
    private String message;

    public SendMessageResponse() {}

    public SendMessageResponse(Long messageId, String status, String sentAt, String message) {
        this.messageId = messageId;
        this.status = status;
        this.sentAt = sentAt;
        this.message = message;
    }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
