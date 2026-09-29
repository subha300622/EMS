package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Response returned after creating or updating an exit request")
public class ExitResponse {

    @Schema(description = "Exit record ID", example = "5001")
    private Long exitId;

    @Schema(description = "Employee ID", example = "1025")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Employee code", example = "EMP-1025")
    private String employeeCode;

    @Schema(description = "Current exit workflow status", example = "MANAGER_APPROVAL_PENDING")
    private String status;

    @Schema(description = "Requested last working date", example = "2026-10-17")
    private LocalDate requestedLastWorkingDate;

    @Schema(description = "Last working date confirmed by HR", example = "2026-10-17")
    private LocalDate lastWorkingDate;

    @Schema(description = "Exit creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public ExitResponse() {}

    public ExitResponse(Long exitId, Long employeeId, String employeeName, String employeeCode, String status, LocalDate requestedLastWorkingDate, LocalDate lastWorkingDate, LocalDateTime createdAt) {
        this.exitId = exitId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.status = status;
        this.requestedLastWorkingDate = requestedLastWorkingDate;
        this.lastWorkingDate = lastWorkingDate;
        this.createdAt = createdAt;
    }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRequestedLastWorkingDate() { return requestedLastWorkingDate; }
    public void setRequestedLastWorkingDate(LocalDate requestedLastWorkingDate) { this.requestedLastWorkingDate = requestedLastWorkingDate; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
