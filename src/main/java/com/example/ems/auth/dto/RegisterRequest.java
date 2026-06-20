package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Schema(example = "John Doe")
    private String fullName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String workEmail;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must be 10–15 digits")
    @Schema(example = "+1-555-0199")
    private String mobileNumber;

    // Optional
    @Schema(example = "string")
    private String employeeId;

    @NotBlank(message = "Department is required")
    @Schema(example = "Engineering")
    private String department;

    // Optional — Admin assigns the actual role after approval
    @Schema(example = "Software Engineer")
    private String requestedRole;

    // Optional
    @Schema(example = "Bangalore")
    private String location;

    @NotBlank(message = "Password is required")
    @Schema(example = "string")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(example = "string")
    private String confirmPassword;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail;
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

    public String getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
