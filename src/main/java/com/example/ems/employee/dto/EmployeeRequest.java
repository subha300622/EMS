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

    @Schema(example = "[\"8\"]")
    private List<Object> roleIds;

    @Schema(example = "[]")
    private List<Object> roleAssignments;

    private Long departmentId;
    private Long designationId;
    private Long teamId;
    private Long teamLeadId;
    private Long locationId;

    private PersonalDetailsDto personalDetails;
    private ContactDetailsDto contactDetails;
    private EmergencyContactDto emergencyContact;
    private BankDetailsDto bankDetails;

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

    @Schema(example = "2026-06-19")
    private LocalDate joiningDate;

    @Schema(example = "Bangalore")
    private String location;

    @Schema(example = "Full-time")
    private String employmentType;

    @Schema(example = "Active")
    private String employeeStatus;

    @Schema(example = "Active")
    private String employmentStatus;

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

    public List<Object> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Object> roleIds) { this.roleIds = roleIds; }

    public List<Object> getRoleAssignments() { return roleAssignments; }
    public void setRoleAssignments(List<Object> roleAssignments) { this.roleAssignments = roleAssignments; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getTeamLeadId() { return teamLeadId; }
    public void setTeamLeadId(Long teamLeadId) { this.teamLeadId = teamLeadId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public PersonalDetailsDto getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(PersonalDetailsDto personalDetails) { this.personalDetails = personalDetails; }

    public ContactDetailsDto getContactDetails() { return contactDetails; }
    public void setContactDetails(ContactDetailsDto contactDetails) { this.contactDetails = contactDetails; }

    public EmergencyContactDto getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(EmergencyContactDto emergencyContact) { this.emergencyContact = emergencyContact; }

    public BankDetailsDto getBankDetails() { return bankDetails; }
    public void setBankDetails(BankDetailsDto bankDetails) { this.bankDetails = bankDetails; }

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

    public String getEmploymentStatus() { return employmentStatus != null ? employmentStatus : employeeStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public String getSourceOfHire() { return sourceOfHire; }
    public void setSourceOfHire(String sourceOfHire) { this.sourceOfHire = sourceOfHire; }

    public String getTotalExperience() { return totalExperience; }
    public void setTotalExperience(String totalExperience) { this.totalExperience = totalExperience; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMaritalStatus() {
        if (personalDetails != null && personalDetails.getMaritalStatus() != null) {
            return personalDetails.getMaritalStatus();
        }
        return maritalStatus;
    }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getBloodGroup() {
        if (personalDetails != null && personalDetails.getBloodGroup() != null) {
            return personalDetails.getBloodGroup();
        }
        return bloodGroup;
    }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getNationality() {
        if (personalDetails != null && personalDetails.getNationality() != null) {
            return personalDetails.getNationality();
        }
        return nationality;
    }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getUanNumber() { return uanNumber; }
    public void setUanNumber(String uanNumber) { this.uanNumber = uanNumber; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

    public String getPersonalMobile() {
        if (contactDetails != null && contactDetails.getPersonalMobile() != null) {
            return contactDetails.getPersonalMobile();
        }
        return personalMobile;
    }
    public void setPersonalMobile(String personalMobile) { this.personalMobile = personalMobile; }

    public String getWorkMobile() {
        if (contactDetails != null && contactDetails.getWorkMobile() != null) {
            return contactDetails.getWorkMobile();
        }
        return workMobile;
    }
    public void setWorkMobile(String workMobile) { this.workMobile = workMobile; }

    public String getCurrentAddress() {
        if (contactDetails != null && contactDetails.getAddress() != null) {
            return contactDetails.getAddress();
        }
        return currentAddress;
    }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    public Boolean getSameAddress() { return sameAddress; }
    public void setSameAddress(Boolean sameAddress) { this.sameAddress = sameAddress; }

    public String getEmergencyContactName() {
        if (emergencyContact != null && emergencyContact.getName() != null) {
            return emergencyContact.getName();
        }
        return emergencyContactName;
    }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactNumber() {
        if (emergencyContact != null && emergencyContact.getPhone() != null) {
            return emergencyContact.getPhone();
        }
        return emergencyContactNumber;
    }
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
        if (contactDetails != null && contactDetails.getWorkMobile() != null) {
            return contactDetails.getWorkMobile();
        }
        if (contactDetails != null && contactDetails.getPersonalMobile() != null) {
            return contactDetails.getPersonalMobile();
        }
        if (phone == null || phone.isBlank() || "string".equalsIgnoreCase(phone)) {
            return personalMobile;
        }
        return phone;
    }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() {
        if (contactDetails != null && contactDetails.getAddress() != null) {
            return contactDetails.getAddress();
        }
        if (address == null || address.isBlank() || "string".equalsIgnoreCase(address)) {
            return currentAddress;
        }
        return address;
    }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() {
        if (employmentStatus != null && !employmentStatus.isBlank()) {
            return employmentStatus;
        }
        if (status == null || status.isBlank() || "string".equalsIgnoreCase(status)) {
            return employeeStatus;
        }
        return status;
    }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getJoiningDate() {
        if (joiningDate != null) {
            return joiningDate;
        }
        return dateOfJoining;
    }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    // Nested DTO classes
    public static class PersonalDetailsDto {
        private String nationality;
        private String maritalStatus;
        private String bloodGroup;

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public String getMaritalStatus() { return maritalStatus; }
        public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

        public String getBloodGroup() { return bloodGroup; }
        public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    }

    public static class ContactDetailsDto {
        private String personalEmail;
        private String workMobile;
        private String personalMobile;
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        public String getPersonalEmail() { return personalEmail; }
        public void setPersonalEmail(String personalEmail) { this.personalEmail = personalEmail; }

        public String getWorkMobile() { return workMobile; }
        public void setWorkMobile(String workMobile) { this.workMobile = workMobile; }

        public String getPersonalMobile() { return personalMobile; }
        public void setPersonalMobile(String personalMobile) { this.personalMobile = personalMobile; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    }

    public static class EmergencyContactDto {
        private String name;
        private String relationship;
        private String countryCode;
        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class BankDetailsDto {
        private String accountHolderName;
        private String accountNumber;
        private String bankName;
        private String branchName;
        private String ifscCode;

        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }

        public String getBranchName() { return branchName; }
        public void setBranchName(String branchName) { this.branchName = branchName; }

        public String getIfscCode() { return ifscCode; }
        public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
    }

}
