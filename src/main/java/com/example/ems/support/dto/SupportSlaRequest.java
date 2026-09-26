package com.example.ems.support.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SupportSlaRequest {

    @NotBlank(message = "Name must not be empty")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Priority must not be empty")
    private String priority;

    @Min(value = 1, message = "First response time must be at least 1 minute")
    private Integer firstResponseMinutes;

    @Min(value = 1, message = "Resolution time must be at least 1 minute")
    private Integer resolutionMinutes;

    private Boolean isDefault = false;

    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    // Legacy fields for backward compatibility with PlatformSupportService
    private int responseTimeMinutes;
    private int resolutionTimeMinutes;
    private boolean businessHoursOnly = false;
    private boolean enabled = true;
    private Integer escalationAfterMinutes;
    private Integer autoCloseAfterDays;
    private Integer warningBeforeMinutes;

    public SupportSlaRequest() {}

    // Getters and setters for new fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Integer getFirstResponseMinutes() {
        return firstResponseMinutes != null ? firstResponseMinutes : responseTimeMinutes;
    }
    public void setFirstResponseMinutes(Integer firstResponseMinutes) {
        this.firstResponseMinutes = firstResponseMinutes;
        if (firstResponseMinutes != null) {
            this.responseTimeMinutes = firstResponseMinutes;
        }
    }

    public Integer getResolutionMinutes() {
        return resolutionMinutes != null ? resolutionMinutes : resolutionTimeMinutes;
    }
    public void setResolutionMinutes(Integer resolutionMinutes) {
        this.resolutionMinutes = resolutionMinutes;
        if (resolutionMinutes != null) {
            this.resolutionTimeMinutes = resolutionMinutes;
        }
    }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.enabled = "ACTIVE".equalsIgnoreCase(status);
    }

    // Getters and setters for legacy fields (ensures compatibility)
    public int getResponseTimeMinutes() {
        return firstResponseMinutes != null ? firstResponseMinutes : responseTimeMinutes;
    }
    public void setResponseTimeMinutes(int responseTimeMinutes) {
        this.responseTimeMinutes = responseTimeMinutes;
        this.firstResponseMinutes = responseTimeMinutes;
    }

    public int getResolutionTimeMinutes() {
        return resolutionMinutes != null ? resolutionMinutes : resolutionTimeMinutes;
    }
    public void setResolutionTimeMinutes(int resolutionTimeMinutes) {
        this.resolutionTimeMinutes = resolutionTimeMinutes;
        this.resolutionMinutes = resolutionTimeMinutes;
    }

    public boolean isBusinessHoursOnly() { return businessHoursOnly; }
    public void setBusinessHoursOnly(boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; }

    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(status) && enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.status = enabled ? "ACTIVE" : "INACTIVE";
    }

    public Integer getEscalationAfterMinutes() { return escalationAfterMinutes; }
    public void setEscalationAfterMinutes(Integer escalationAfterMinutes) { this.escalationAfterMinutes = escalationAfterMinutes; }

    public Integer getAutoCloseAfterDays() { return autoCloseAfterDays; }
    public void setAutoCloseAfterDays(Integer autoCloseAfterDays) { this.autoCloseAfterDays = autoCloseAfterDays; }

    public Integer getWarningBeforeMinutes() { return warningBeforeMinutes; }
    public void setWarningBeforeMinutes(Integer warningBeforeMinutes) { this.warningBeforeMinutes = warningBeforeMinutes; }
}
