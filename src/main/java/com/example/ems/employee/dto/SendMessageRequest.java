package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class SendMessageRequest {
    @Schema(example = "Request for Leave")
    private String subject;
    @Schema(example = "string")
    private String message;

    public SendMessageRequest() {}

    public SendMessageRequest(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
