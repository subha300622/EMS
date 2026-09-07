package com.example.ems.appraisal.dto;

import java.util.ArrayList;
import java.util.List;

public class AppraisalConfigurationValidationResponseDto {

    private boolean valid;
    private List<ValidationErrorDetailDto> errors = new ArrayList<>();

    public AppraisalConfigurationValidationResponseDto() {}

    public AppraisalConfigurationValidationResponseDto(boolean valid, List<ValidationErrorDetailDto> errors) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public List<ValidationErrorDetailDto> getErrors() { return errors; }
    public void setErrors(List<ValidationErrorDetailDto> errors) { this.errors = errors; }
}
