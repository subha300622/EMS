package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public class MyDocumentsDashboardResponse {

    private EmployeeInfo employee;
    private SummaryInfo summary;
    private List<AlertInfo> alerts;

    public MyDocumentsDashboardResponse() {}

    public MyDocumentsDashboardResponse(EmployeeInfo employee, SummaryInfo summary, List<AlertInfo> alerts) {
        this.employee = employee;
        this.summary = summary;
        this.alerts = alerts;
    }

    public EmployeeInfo getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeInfo employee) {
        this.employee = employee;
    }

    public SummaryInfo getSummary() {
        return summary;
    }

    public void setSummary(SummaryInfo summary) {
        this.summary = summary;
    }

    public List<AlertInfo> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertInfo> alerts) {
        this.alerts = alerts;
    }

    public static class EmployeeInfo {
        @Schema(example = "1")
        private Long id;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "string")
        private String name;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String employeeCode, String name) {
            this.id = id;
            this.employeeCode = employeeCode;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class SummaryInfo {
        @Schema(example = "1")
        private int totalDocuments;
        @Schema(example = "1")
        private int uploadedDocuments;
        @Schema(example = "1")
        private int pendingDocuments;
        @Schema(example = "1")
        private int expiringSoonDocuments;
        @Schema(example = "1")
        private int completionPercentage;

        public SummaryInfo() {}

        public SummaryInfo(int totalDocuments, int uploadedDocuments, int pendingDocuments, int expiringSoonDocuments, int completionPercentage) {
            this.totalDocuments = totalDocuments;
            this.uploadedDocuments = uploadedDocuments;
            this.pendingDocuments = pendingDocuments;
            this.expiringSoonDocuments = expiringSoonDocuments;
            this.completionPercentage = completionPercentage;
        }

        public int getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(int totalDocuments) { this.totalDocuments = totalDocuments; }
        public int getUploadedDocuments() { return uploadedDocuments; }
        public void setUploadedDocuments(int uploadedDocuments) { this.uploadedDocuments = uploadedDocuments; }
        public int getPendingDocuments() { return pendingDocuments; }
        public void setPendingDocuments(int pendingDocuments) { this.pendingDocuments = pendingDocuments; }
        public int getExpiringSoonDocuments() { return expiringSoonDocuments; }
        public void setExpiringSoonDocuments(int expiringSoonDocuments) { this.expiringSoonDocuments = expiringSoonDocuments; }
        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    }

    public static class AlertInfo {
        @Schema(example = "string")
        private String type;
        @Schema(example = "string")
        private String message;
        @Schema(example = "1")
        private Integer count;
        @Schema(example = "1")
        private Long documentId;
        @Schema(example = "2026-06-19")
        private LocalDate expiryDate;
        @Schema(example = "string")
        private String severity;

        public AlertInfo() {}

        public AlertInfo(String type, String message, Integer count) {
            this.type = type;
            this.message = message;
            this.count = count;
        }

        public AlertInfo(String type, String message, Long documentId, LocalDate expiryDate, String severity) {
            this.type = type;
            this.message = message;
            this.documentId = documentId;
            this.expiryDate = expiryDate;
            this.severity = severity;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
