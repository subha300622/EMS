package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Schema(example = "John Doe")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must be 10–15 digits")
    @Schema(example = "+1-555-0199")
    private String mobileNumber;

    @Schema(example = "string")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Schema(example = "Engineering")
    private String department;

    @Schema(example = "Bangalore")
    private String location;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
