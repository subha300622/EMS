package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Summary DTO for offboarding and exit requests on the dashboard")
public class OffboardingRequestSummaryDto {

    @Schema(description = "Offboarding record ID", example = "1")
    private Long id;

    @Schema(description = "Employee database ID", example = "25")
    private Long employeeId;

    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Employee company code", example = "EMP025")
    private String employeeCode;

    @Schema(description = "Employee designation", example = "Senior Software Engineer")
    private String designation;

    @Schema(description = "Employee department", example = "Engineering")
    private String department;

    @Schema(description = "Exit type / Reason category", example = "RESIGNATION")
    private String exitType;

    @Schema(description = "Offboarding status", example = "ACTIVE")
    private String status;

    @Schema(description = "Formal resignation date", example = "2026-09-01")
    private LocalDate resignationDate;

    @Schema(description = "Confirmed or requested last working date", example = "2026-09-30")
    private LocalDate lastWorkingDate;

    @Schema(description = "Current operational stage of offboarding", example = "FINANCE_CLEARANCE")
    private String currentStage;

    public OffboardingRequestSummaryDto() {}

    public OffboardingRequestSummaryDto(Long id, Long employeeId, String employeeName, String employeeCode,
                                       String designation, String department, String exitType, String status,
                                       LocalDate resignationDate, LocalDate lastWorkingDate, String currentStage) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeCode = employeeCode;
        this.designation = designation;
        this.department = department;
        this.exitType = exitType;
        this.status = status;
        this.resignationDate = resignationDate;
        this.lastWorkingDate = lastWorkingDate;
        this.currentStage = currentStage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getExitType() { return exitType; }
    public void setExitType(String exitType) { this.exitType = exitType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getResignationDate() { return resignationDate; }
    public void setResignationDate(LocalDate resignationDate) { this.resignationDate = resignationDate; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
}
