package com.example.ems.recruitment.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CandidateConversionRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long managerId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
}
