package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEligibilityDetailDto {

    private Long employeeId;
    private String employeeName;
    private String department;
    private String designation;
    private LocalDate joiningDate;
    private Integer serviceMonths;
    private boolean eligible;
    private String ineligibilityReason;
    private String reason;
}
