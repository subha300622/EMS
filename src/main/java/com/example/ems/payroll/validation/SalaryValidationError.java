package com.example.ems.payroll.validation;

public class SalaryValidationError {

    private String componentCode;
    private Long componentId;
    private SalaryValidationErrorType errorType;
    private String message;

    public SalaryValidationError() {}

    public SalaryValidationError(String componentCode, Long componentId, SalaryValidationErrorType errorType, String message) {
        this.componentCode = componentCode;
        this.componentId = componentId;
        this.errorType = errorType;
        this.message = message;
    }

    public static SalaryValidationError of(SalaryValidationErrorType errorType, String message) {
        return new SalaryValidationError(null, null, errorType, message);
    }

    public static SalaryValidationError ofComponent(String componentCode, Long componentId, SalaryValidationErrorType errorType, String message) {
        return new SalaryValidationError(componentCode, componentId, errorType, message);
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public SalaryValidationErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(SalaryValidationErrorType errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
