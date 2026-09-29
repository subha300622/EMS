package com.example.ems.employee.dto;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public class ChangeEmployeeRoleRequest {
    @NotEmpty(message = "Role IDs list cannot be empty")
    private List<Long> roleIds;

    private LocalDate effectiveDate;
    private String reason;

    public ChangeEmployeeRoleRequest() {}
    public ChangeEmployeeRoleRequest(List<Long> roleIds, LocalDate effectiveDate, String reason) {
        this.roleIds = roleIds;
        this.effectiveDate = effectiveDate;
        this.reason = reason;
    }

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
