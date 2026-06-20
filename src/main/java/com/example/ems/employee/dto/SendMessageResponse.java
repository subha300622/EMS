package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class SendMessageResponse {
    @Schema(example = "1")
    private Long messageId;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "string")
    private String sentAt;
    @Schema(example = "string")
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
