package com.example.ems.appraisal.dto;

import java.time.LocalDateTime;

public class AppraisalHistoryResponseDto {
    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private String changeType;
    private String oldValue;
    private String newValue;
    private Long changedById;
    private String changedByName;
    private String comments;
    private LocalDateTime changedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Long getChangedById() { return changedById; }
    public void setChangedById(Long changedById) { this.changedById = changedById; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
