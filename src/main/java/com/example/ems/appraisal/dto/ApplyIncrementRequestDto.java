package com.example.ems.appraisal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyIncrementRequestDto {
    private LocalDate effectiveDate;
    private String remarks;
}
