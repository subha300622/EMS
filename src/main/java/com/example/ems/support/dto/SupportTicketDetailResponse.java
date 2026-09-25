package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public class SupportTicketDetailResponse {

    private Long id;
    private String ticketNumber;
    private String subject;
    private String description;
    private CategoryInfo category;
    private String priority;
    private Double estimatedHours;
    private Double actualHours;
    private Integer slaHours;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private String dueDate;

    private UserInfo assignedTo;
    private String status;
    private Boolean isOverdue;
    private Boolean isEscalated;
    private Integer escalationLevel;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private String createdAt;

    public SupportTicketDetailResponse() {}

    public static class CategoryInfo {
        private Long id;
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

    public static class UserInfo {
        private Long id;
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
