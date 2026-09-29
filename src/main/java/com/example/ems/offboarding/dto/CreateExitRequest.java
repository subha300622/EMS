package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Request to create an employee exit / resignation")
public class CreateExitRequest {

    @NotNull(message = "Employee ID is required")
    @Schema(description = "Database ID of the exiting employee", example = "1025", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long employeeId;

    @Schema(description = "Type of exit: RESIGNATION, TERMINATION, RETIREMENT", example = "RESIGNATION")
    private String exitType = "RESIGNATION";

    @Schema(description = "Date resignation was submitted", example = "2026-09-17")
    private LocalDate resignationDate;

    @Schema(description = "Requested last working date by employee", example = "2026-10-17")
    private LocalDate requestedLastWorkingDate;

    @Schema(description = "Reason for leaving", example = "Personal reasons")
    private String reason;

    @Schema(description = "Additional remarks or comments", example = "Employee submitted resignation")
    private String remarks;

    public CreateExitRequest() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getExitType() { return exitType; }
    public void setExitType(String exitType) { this.exitType = exitType; }

    public LocalDate getResignationDate() { return resignationDate; }
    public void setResignationDate(LocalDate resignationDate) { this.resignationDate = resignationDate; }

    public LocalDate getRequestedLastWorkingDate() { return requestedLastWorkingDate; }
    public void setRequestedLastWorkingDate(LocalDate requestedLastWorkingDate) { this.requestedLastWorkingDate = requestedLastWorkingDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
