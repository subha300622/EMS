package com.example.ems.appraisal.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalaryRevisionApplyResponse {

    @Schema(example = "string")
    private String revisionId;
    @Schema(example = "string")
    private String employeeId;
    @Schema(example = "120000.00")
    private BigDecimal previousSalary;
    @Schema(example = "120000.00")
    private BigDecimal newSalary;
    @Schema(example = "string")
    private String appliedAt;
    @Schema(example = "ACTIVE")
    private String status;

    public SalaryRevisionApplyResponse() {}

    public SalaryRevisionApplyResponse(String revisionId, String employeeId, BigDecimal previousSalary, BigDecimal newSalary, String appliedAt, String status) {
        this.revisionId = revisionId;
        this.employeeId = employeeId;
        this.previousSalary = previousSalary;
        this.newSalary = newSalary;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public BigDecimal getPreviousSalary() { return previousSalary; }
    public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }

    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }

    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
