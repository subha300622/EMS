package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EmployeeRequest {

    @Schema(example = "John")
    private String firstName;

    @Schema(example = "Doe")
    private String lastName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(example = "john.doe@example.com")
    private String email;

    @Schema(example = "string")
    private String employeeId;

    @Schema(example = "password123")
    private String password;

    @Schema(example = "password123")
    private String confirmPassword;

    @Schema(example = "[\"ROLE_EMPLOYEE\"]")
    private List<String> roleIds;

    @Schema(example = "[]")
    private List<Object> roleAssignments;

    @Schema(example = "1")
    private String reportingManager;

    @Schema(example = "Engineering")
    private String department;

    @Schema(example = "Software Engineer")
    private String designation;

    @Schema(example = "120000.00")
    private BigDecimal annualSalary;

    @Schema(example = "2026-06-19")
    private LocalDate dateOfJoining;

    @Schema(example = "Bangalore")
    private String location;

    @Schema(example = "Full-time")
    private String employmentType;

    @Schema(example = "Active")
    private String employeeStatus;

    @Schema(example = "Referral")
    private String sourceOfHire;

    @Schema(example = "5 years")
    private String totalExperience;

    @Schema(example = "1990-01-15")
    private LocalDate dob;

    @Schema(example = "Male")
    private String gender;

    @Schema(example = "Single")
    private String maritalStatus;

    @Schema(example = "O+")
    private String bloodGroup;

    @Schema(example = "Indian")
    private String nationality;

    @Schema(example = "123456789012")
    private String aadhaarNumber;

    @Schema(example = "ABCDE1234F")
    private String panNumber;

    @Schema(example = "100123456789")
    private String uanNumber;

    @Schema(example = "A1234567")
    private String passportNumber;

    @Schema(example = "+91 9999999999")
    private String personalMobile;

    @Schema(example = "+91 8888888888")
    private String workMobile;

    @Schema(example = "123 Main St, Springfield")
    private String currentAddress;

    @Schema(example = "123 Main St, Springfield")
    private String permanentAddress;

    @Schema(example = "false")
    private Boolean sameAddress;

    @Schema(example = "Jane Doe")
    private String emergencyContactName;

    @Schema(example = "9876543210")
    private String emergencyContactNumber;

    @Schema(example = "2026-12-19")
    private LocalDate probationEndDate;

    @Schema(example = "Some notes here")
    private String notes;

    @Schema(example = "true")
    private Boolean sendInvite;

    @Schema(example = "true")
    private Boolean notifyManager;

    @Schema(example = "false")
    private Boolean notifyHR;

    @Schema(example = "true")
    private Boolean reminderUnopened;

    // Existing fields fallback compatibility
    private String fullName;
    private String phone;
    private String address;
    private String status;
    private LocalDate joiningDate;
    private Long managerId;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmployeeId() {
        if ("string".equalsIgnoreCase(employeeId) || (employeeId != null && employeeId.isBlank())) {
            return null;
        }
        return employeeId;
    }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public List<String> getRoleIds() { return roleIds; }
    public void setRoleIds(List<String> roleIds) { this.roleIds = roleIds; }

    public List<Object> getRoleAssignments() { return roleAssignments; }
    public void setRoleAssignments(List<Object> roleAssignments) { this.roleAssignments = roleAssignments; }

    public String getReportingManager() { return reportingManager; }
    public void setReportingManager(String reportingManager) { this.reportingManager = reportingManager; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }

    public LocalDate getDateOfJoining() { return dateOfJoining; }
    public void setDateOfJoining(LocalDate dateOfJoining) { this.dateOfJoining = dateOfJoining; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getEmployeeStatus() { return employeeStatus; }
    public void setEmployeeStatus(String employeeStatus) { this.employeeStatus = employeeStatus; }

    public String getSourceOfHire() { return sourceOfHire; }
    public void setSourceOfHire(String sourceOfHire) { this.sourceOfHire = sourceOfHire; }

    public String getTotalExperience() { return totalExperience; }
    public void setTotalExperience(String totalExperience) { this.totalExperience = totalExperience; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getUanNumber() { return uanNumber; }
    public void setUanNumber(String uanNumber) { this.uanNumber = uanNumber; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

    public String getPersonalMobile() { return personalMobile; }
    public void setPersonalMobile(String personalMobile) { this.personalMobile = personalMobile; }

    public String getWorkMobile() { return workMobile; }
    public void setWorkMobile(String workMobile) { this.workMobile = workMobile; }

    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    public Boolean getSameAddress() { return sameAddress; }
    public void setSameAddress(Boolean sameAddress) { this.sameAddress = sameAddress; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }

    public LocalDate getProbationEndDate() { return probationEndDate; }
    public void setProbationEndDate(LocalDate probationEndDate) { this.probationEndDate = probationEndDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getSendInvite() { return sendInvite; }
    public void setSendInvite(Boolean sendInvite) { this.sendInvite = sendInvite; }

    public Boolean getNotifyManager() { return notifyManager; }
    public void setNotifyManager(Boolean notifyManager) { this.notifyManager = notifyManager; }

    public Boolean getNotifyHR() { return notifyHR; }
    public void setNotifyHR(Boolean notifyHR) { this.notifyHR = notifyHR; }

    public Boolean getReminderUnopened() { return reminderUnopened; }
    public void setReminderUnopened(Boolean reminderUnopened) { this.reminderUnopened = reminderUnopened; }

    // Getters/setters for legacy compat fields
    public String getFullName() {
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim() + (lastName != null && !lastName.isBlank() ? " " + lastName.trim() : "");
        }
        if (fullName == null || fullName.isBlank() || "string".equalsIgnoreCase(fullName)) {
            return "";
        }
        return fullName;
    }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() {
        if (phone == null || phone.isBlank() || "string".equalsIgnoreCase(phone)) {
            return personalMobile;
        }
        return phone;
    }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() {
        if (address == null || address.isBlank() || "string".equalsIgnoreCase(address)) {
            return currentAddress;
        }
        return address;
    }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() {
        if (status == null || status.isBlank() || "string".equalsIgnoreCase(status)) {
            return employeeStatus;
        }
        return status;
    }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getJoiningDate() {
        if (dateOfJoining != null) {
            return dateOfJoining;
        }
        return joiningDate;
    }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}
