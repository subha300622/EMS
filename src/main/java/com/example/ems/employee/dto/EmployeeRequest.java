package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequest {

    @NotBlank(message = "Full name is required")
    @Schema(example = "John Doe")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;

    @Schema(example = "string")
    private String employeeId;

    @Schema(example = "+1-555-0199")
    private String phone;

    @Schema(example = "MALE")
    private String gender;

    @Schema(example = "1990-01-15")
    private LocalDate dob;

    @Schema(example = "123 Main St, Springfield")
    private String address;

    @NotBlank(message = "Department is required")
    @Schema(example = "Engineering")
    private String department;

    @NotBlank(message = "Designation is required")
    @Schema(example = "Software Engineer")
    private String designation;

    @NotNull(message = "Annual salary is required")
    @Positive(message = "Annual salary must be positive")
    @Schema(example = "120000.00")
    private BigDecimal annualSalary;

    @NotNull(message = "Joining date is required")
    @Schema(example = "2026-06-19")
    private LocalDate joiningDate;

    @Schema(example = "Bangalore")
    private String location;

    @Schema(example = "FULL_TIME")
    private String employmentType;

    @Schema(example = "ACTIVE")
    private String status;

    @Schema(example = "1")
    private Long managerId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getAnnualSalary() {
        return annualSalary;
    }

    public void setAnnualSalary(BigDecimal annualSalary) {
        this.annualSalary = annualSalary;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }
}
