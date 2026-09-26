package com.example.ems.recruitment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class OfferGenerateRequest {

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Annual salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual salary must be greater than 0")
    private BigDecimal annualSalary;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @Min(value = 0, message = "Probation months cannot be negative")
    private Integer probationMonths = 6;

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Integer getProbationMonths() { return probationMonths; }
    public void setProbationMonths(Integer probationMonths) { this.probationMonths = probationMonths; }
}
