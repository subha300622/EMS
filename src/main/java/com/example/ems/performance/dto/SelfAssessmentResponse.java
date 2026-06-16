package com.example.ems.performance.dto;

import java.util.List;

public class SelfAssessmentResponse {
    private String message;
    private String status;
    private String submittedAt;
    private List<String> summary;

    public SelfAssessmentResponse(String message, String status, String submittedAt, List<String> summary) {
        this.message = message;
        this.status = status;
        this.submittedAt = submittedAt;
        this.summary = summary;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
    public List<String> getSummary() { return summary; }
    public void setSummary(List<String> summary) { this.summary = summary; }
}
