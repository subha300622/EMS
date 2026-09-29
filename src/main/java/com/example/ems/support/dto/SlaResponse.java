package com.example.ems.support.dto;

import com.example.ems.support.entity.SupportSla;
import java.time.LocalDateTime;

public class SlaResponse {

    private Long id;
    private String name;
    private String description;
    private PriorityDto priority;
    private DurationDto firstResponse;
    private DurationDto resolution;
    private StatusDto status;
    private boolean isDefault;
    private StatisticsDto statistics;
    private AuditDto audit;

    public SlaResponse() {}

    public SlaResponse(SupportSla sla, long assigned, long breached, double compliance) {
        this.id = sla.getId();
        this.name = sla.getName();
        this.description = sla.getDescription();
        this.isDefault = sla.isDefault();
        
        // Priority
        String priorityCode = sla.getPriority().name();
        String priorityLabel = priorityCode.substring(0, 1) + priorityCode.substring(1).toLowerCase();
        String color = switch (sla.getPriority()) {
            case CRITICAL -> "#DC2626";
            case HIGH -> "#F97316";
            case MEDIUM -> "#EAB308";
            case LOW -> "#3B82F6";
        };
        this.priority = new PriorityDto(priorityCode, priorityLabel, color);

        // First Response
        int responseVal = sla.getResponseTimeMinutes();
        String responseDisplay = responseVal >= 60 ? (responseVal / 60) + " Hours" : responseVal + " Minutes";
        this.firstResponse = new DurationDto(responseVal, "MINUTES", responseDisplay);

        // Resolution
        int resVal = sla.getResolutionTimeMinutes();
        String resDisplay = resVal >= 60 ? (resVal / 60) + " Hours" : resVal + " Minutes";
        this.resolution = new DurationDto(resVal, "MINUTES", resDisplay);

        // Status
        this.status = new StatusDto(sla.isEnabled() ? "ACTIVE" : "INACTIVE", sla.isEnabled() ? "Active" : "Inactive");

        // Statistics
        this.statistics = new StatisticsDto(assigned, breached, Math.round(compliance * 100.0) / 100.0);

        // Audit
        UserDto createdByDto = sla.getCreatedBy() != null ? new UserDto(sla.getCreatedBy().getUserId(), sla.getCreatedBy().getFullName()) : null;
        UserDto updatedByDto = sla.getUpdatedBy() != null ? new UserDto(sla.getUpdatedBy().getUserId(), sla.getUpdatedBy().getFullName()) : null;
        this.audit = new AuditDto(createdByDto, updatedByDto, sla.getCreatedAt(), sla.getUpdatedAt());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PriorityDto getPriority() { return priority; }
    public void setPriority(PriorityDto priority) { this.priority = priority; }

    public DurationDto getFirstResponse() { return firstResponse; }
    public void setFirstResponse(DurationDto firstResponse) { this.firstResponse = firstResponse; }

    public DurationDto getResolution() { return resolution; }
    public void setResolution(DurationDto resolution) { this.resolution = resolution; }

    public StatusDto getStatus() { return status; }
    public void setStatus(StatusDto status) { this.status = status; }

    public boolean getIsDefault() { return isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }

    public StatisticsDto getStatistics() { return statistics; }
    public void setStatistics(StatisticsDto statistics) { this.statistics = statistics; }

    public AuditDto getAudit() { return audit; }
    public void setAudit(AuditDto audit) { this.audit = audit; }

    public static class PriorityDto {
        private String code;
        private String label;
        private String color;

        public PriorityDto(String code, String label, String color) {
            this.code = code;
            this.label = label;
            this.color = color;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    public static class DurationDto {
        private int value;
        private String unit;
        private String displayValue;

        public DurationDto(int value, String unit, String displayValue) {
            this.value = value;
            this.unit = unit;
            this.displayValue = displayValue;
        }

        public int getValue() { return value; }
        public String getUnit() { return unit; }
        public String getDisplayValue() { return displayValue; }
    }

    public static class StatusDto {
        private String code;
        private String label;

        public StatusDto(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
    }

    public static class StatisticsDto {
        private long assignedTickets;
        private long breachedTickets;
        private double compliancePercentage;

        public StatisticsDto(long assignedTickets, long breachedTickets, double compliancePercentage) {
            this.assignedTickets = assignedTickets;
            this.breachedTickets = breachedTickets;
            this.compliancePercentage = compliancePercentage;
        }

        public long getAssignedTickets() { return assignedTickets; }
        public long getBreachedTickets() { return breachedTickets; }
        public double getCompliancePercentage() { return compliancePercentage; }
    }

    public static class UserDto {
        private String id;
        private String name;

        public UserDto(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class AuditDto {
        private UserDto createdBy;
        private UserDto updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AuditDto(UserDto createdBy, UserDto updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.createdBy = createdBy;
            this.updatedBy = updatedBy;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public UserDto getCreatedBy() { return createdBy; }
        public UserDto getUpdatedBy() { return updatedBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }
}
