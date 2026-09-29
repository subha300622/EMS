package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Support Ticket Details Response")
public class SupportTicketDetailResponse {

    @Schema(description = "Ticket ID", example = "101")
    private Long id;

    @Schema(description = "Ticket tracking number", example = "TICK-2026-0001")
    private String ticketNumber;

    @Schema(description = "Summary subject of the ticket", example = "VPN connection fails intermittently")
    private String subject;

    @Schema(description = "Detailed issue description", example = "Unable to connect to internal VPN since morning.")
    private String description;

    @Schema(description = "Assigned Support Category")
    private CategoryInfo category;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;

    @Schema(description = "Estimated hours to resolve", example = "4.0")
    private Double estimatedHours;

    @Schema(description = "Actual hours logged", example = "1.5")
    private Double actualHours;

    @Schema(description = "SLA resolution time in hours", example = "8")
    private Integer slaHours;

    @Schema(description = "SLA Due Date", example = "2026-09-30T18:00:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private String dueDate;

    @Schema(description = "Assigned support engineer")
    private UserInfo assignedTo;

    @Schema(description = "Current ticket status", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "Whether the ticket has breached its SLA", example = "false")
    private Boolean isOverdue;

    @Schema(description = "Whether the ticket is escalated", example = "false")
    private Boolean isEscalated;

    @Schema(description = "Current escalation level (0 if unescalated)", example = "0")
    private Integer escalationLevel;

    @Schema(description = "Creation timestamp", example = "2026-09-25T10:00:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private String createdAt;

    public SupportTicketDetailResponse() {}

    @Schema(description = "Category Information")
    public static class CategoryInfo {
        @Schema(description = "Category ID", example = "1")
        private Long id;

        @Schema(description = "Category Name", example = "IT & Infrastructure")
        private String name;

        public CategoryInfo() {}
        public CategoryInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Schema(description = "User Information")
    public static class UserInfo {
        @Schema(description = "User ID", example = "42")
        private Long id;

        @Schema(description = "User Full Name", example = "Jane Doe")
        private String name;

        public UserInfo() {}
        public UserInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CategoryInfo getCategory() { return category; }
    public void setCategory(CategoryInfo category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public Double getActualHours() { return actualHours; }
    public void setActualHours(Double actualHours) { this.actualHours = actualHours; }

    public Integer getSlaHours() { return slaHours; }
    public void setSlaHours(Integer slaHours) { this.slaHours = slaHours; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public UserInfo getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UserInfo assignedTo) { this.assignedTo = assignedTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean overdue) { isOverdue = overdue; }

    public Boolean getIsEscalated() { return isEscalated; }
    public void setIsEscalated(Boolean escalated) { isEscalated = escalated; }

    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
