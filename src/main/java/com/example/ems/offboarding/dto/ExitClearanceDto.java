package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Department Clearance details")
public class ExitClearanceDto {

    private Long clearanceId;
    private Long exitId;
    private String department;
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private String clearanceReason;
    private String status; // PENDING, COMPLETED, REJECTED, HOLD
    private Long clearedById;
    private String clearedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime clearedAt;

    private String remarks;
    private Object clearanceData;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public ExitClearanceDto() {}

    public Long getClearanceId() { return clearanceId; }
    public void setClearanceId(Long clearanceId) { this.clearanceId = clearanceId; }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getAssignedToEmail() { return assignedToEmail; }
    public void setAssignedToEmail(String assignedToEmail) { this.assignedToEmail = assignedToEmail; }

    public String getClearanceReason() { return clearanceReason; }
    public void setClearanceReason(String clearanceReason) { this.clearanceReason = clearanceReason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getClearedById() { return clearedById; }
    public void setClearedById(Long clearedById) { this.clearedById = clearedById; }

    public String getClearedByName() { return clearedByName; }
    public void setClearedByName(String clearedByName) { this.clearedByName = clearedByName; }

    public LocalDateTime getClearedAt() { return clearedAt; }
    public void setClearedAt(LocalDateTime clearedAt) { this.clearedAt = clearedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Object getClearanceData() { return clearanceData; }
    public void setClearanceData(Object clearanceData) { this.clearanceData = clearanceData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
