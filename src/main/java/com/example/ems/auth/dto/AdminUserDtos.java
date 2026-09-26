package com.example.ems.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminUserDtos {

    public static class CreateAdminUserRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String employeeId;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;

        @NotBlank(message = "Role ID is required")
        private String roleId;

        @NotBlank(message = "Department ID is required")
        private String departmentId;

        @NotBlank(message = "Designation ID is required")
        private String designationId;

        @NotBlank(message = "Job Level ID is required")
        private String jobLevelId;

        @NotBlank(message = "Employment Type ID is required")
        private String employmentTypeId;

        private String reportingManagerId;

        @NotNull(message = "Work information is required")
        @Valid
        private WorkInformation workInformation;

        @NotNull(message = "Personal information is required")
        @Valid
        private PersonalInformation personalInformation;

        @NotNull(message = "Identity information is required")
        @Valid
        private IdentityInformation identityInformation;

        @NotNull(message = "Notification preferences is required")
        @Valid
        private NotificationPreferences notificationPreferences;

        public CreateAdminUserRequest() {}

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }

        public String getDepartmentId() { return departmentId; }
        public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

        public String getDesignationId() { return designationId; }
        public void setDesignationId(String designationId) { this.designationId = designationId; }

        public String getJobLevelId() { return jobLevelId; }
        public void setJobLevelId(String jobLevelId) { this.jobLevelId = jobLevelId; }

        public String getEmploymentTypeId() { return employmentTypeId; }
        public void setEmploymentTypeId(String employmentTypeId) { this.employmentTypeId = employmentTypeId; }

        public String getReportingManagerId() { return reportingManagerId; }
        public void setReportingManagerId(String reportingManagerId) { this.reportingManagerId = reportingManagerId; }

        public WorkInformation getWorkInformation() { return workInformation; }
        public void setWorkInformation(WorkInformation workInformation) { this.workInformation = workInformation; }

        public PersonalInformation getPersonalInformation() { return personalInformation; }
        public void setPersonalInformation(PersonalInformation personalInformation) { this.personalInformation = personalInformation; }

        public IdentityInformation getIdentityInformation() { return identityInformation; }
        public void setIdentityInformation(IdentityInformation identityInformation) { this.identityInformation = identityInformation; }

        public NotificationPreferences getNotificationPreferences() { return notificationPreferences; }
        public void setNotificationPreferences(NotificationPreferences notificationPreferences) { this.notificationPreferences = notificationPreferences; }

        public static CreateAdminUserRequestBuilder builder() { return new CreateAdminUserRequestBuilder(); }

        public static class CreateAdminUserRequestBuilder {
            private final CreateAdminUserRequest req = new CreateAdminUserRequest();
            public CreateAdminUserRequestBuilder firstName(String firstName) { req.firstName = firstName; return this; }
            public CreateAdminUserRequestBuilder lastName(String lastName) { req.lastName = lastName; return this; }
            public CreateAdminUserRequestBuilder employeeId(String employeeId) { req.employeeId = employeeId; return this; }
            public CreateAdminUserRequestBuilder email(String email) { req.email = email; return this; }
            public CreateAdminUserRequestBuilder password(String password) { req.password = password; return this; }
            public CreateAdminUserRequestBuilder confirmPassword(String confirmPassword) { req.confirmPassword = confirmPassword; return this; }
            public CreateAdminUserRequestBuilder roleId(String roleId) { req.roleId = roleId; return this; }
            public CreateAdminUserRequestBuilder departmentId(String departmentId) { req.departmentId = departmentId; return this; }
            public CreateAdminUserRequestBuilder designationId(String designationId) { req.designationId = designationId; return this; }
            public CreateAdminUserRequestBuilder jobLevelId(String jobLevelId) { req.jobLevelId = jobLevelId; return this; }
            public CreateAdminUserRequestBuilder employmentTypeId(String employmentTypeId) { req.employmentTypeId = employmentTypeId; return this; }
            public CreateAdminUserRequestBuilder reportingManagerId(String reportingManagerId) { req.reportingManagerId = reportingManagerId; return this; }
            public CreateAdminUserRequestBuilder workInformation(WorkInformation workInformation) { req.workInformation = workInformation; return this; }
            public CreateAdminUserRequestBuilder personalInformation(PersonalInformation personalInformation) { req.personalInformation = personalInformation; return this; }
            public CreateAdminUserRequestBuilder identityInformation(IdentityInformation identityInformation) { req.identityInformation = identityInformation; return this; }
            public CreateAdminUserRequestBuilder notificationPreferences(NotificationPreferences notificationPreferences) { req.notificationPreferences = notificationPreferences; return this; }
            public CreateAdminUserRequest build() { return req; }
        }
        public static class Builder extends CreateAdminUserRequestBuilder {}
    }

    public static class WorkInformation {
        private String locationId;
        private String employeeStatus;
        private String sourceOfHire;
        private String dateOfJoining;
        private Integer totalExperienceYears;

        public WorkInformation() {}

        public String getLocationId() { return locationId; }
        public void setLocationId(String locationId) { this.locationId = locationId; }

        public String getEmployeeStatus() { return employeeStatus; }
        public void setEmployeeStatus(String employeeStatus) { this.employeeStatus = employeeStatus; }

        public String getSourceOfHire() { return sourceOfHire; }
        public void setSourceOfHire(String sourceOfHire) { this.sourceOfHire = sourceOfHire; }

        public String getDateOfJoining() { return dateOfJoining; }
        public void setDateOfJoining(String dateOfJoining) { this.dateOfJoining = dateOfJoining; }

        public Integer getTotalExperienceYears() { return totalExperienceYears; }
        public void setTotalExperienceYears(Integer totalExperienceYears) { this.totalExperienceYears = totalExperienceYears; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final WorkInformation wi = new WorkInformation();
            public Builder locationId(String locationId) { wi.locationId = locationId; return this; }
            public Builder employeeStatus(String employeeStatus) { wi.employeeStatus = employeeStatus; return this; }
            public Builder sourceOfHire(String sourceOfHire) { wi.sourceOfHire = sourceOfHire; return this; }
            public Builder dateOfJoining(String dateOfJoining) { wi.dateOfJoining = dateOfJoining; return this; }
            public Builder totalExperienceYears(Integer totalExperienceYears) { wi.totalExperienceYears = totalExperienceYears; return this; }
            public WorkInformation build() { return wi; }
        }
    }

    public static class PersonalInformation {
        private String dateOfBirth;
        private String gender;
        private String maritalStatus;
        private String bloodGroup;
        private String nationality;

        public PersonalInformation() {}

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getMaritalStatus() { return maritalStatus; }
        public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

        public String getBloodGroup() { return bloodGroup; }
        public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final PersonalInformation pi = new PersonalInformation();
            public Builder dateOfBirth(String dateOfBirth) { pi.dateOfBirth = dateOfBirth; return this; }
            public Builder gender(String gender) { pi.gender = gender; return this; }
            public Builder maritalStatus(String maritalStatus) { pi.maritalStatus = maritalStatus; return this; }
            public Builder bloodGroup(String bloodGroup) { pi.bloodGroup = bloodGroup; return this; }
            public Builder nationality(String nationality) { pi.nationality = nationality; return this; }
            public PersonalInformation build() { return pi; }
        }
    }

    public static class IdentityInformation {
        private String aadhaarNumber;
        private String panNumber;
        private String uanNumber;
        private String passportNumber;

        public IdentityInformation() {}

        public String getAadhaarNumber() { return aadhaarNumber; }
        public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

        public String getPanNumber() { return panNumber; }
        public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

        public String getUanNumber() { return uanNumber; }
        public void setUanNumber(String uanNumber) { this.uanNumber = uanNumber; }

        public String getPassportNumber() { return passportNumber; }
        public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final IdentityInformation ii = new IdentityInformation();
            public Builder aadhaarNumber(String aadhaarNumber) { ii.aadhaarNumber = aadhaarNumber; return this; }
            public Builder panNumber(String panNumber) { ii.panNumber = panNumber; return this; }
            public Builder uanNumber(String uanNumber) { ii.uanNumber = uanNumber; return this; }
            public Builder passportNumber(String passportNumber) { ii.passportNumber = passportNumber; return this; }
            public IdentityInformation build() { return ii; }
        }
    }

    public static class NotificationPreferences {
        private Boolean sendInviteEmail;
        private Boolean notifyReportingManager;
        private Boolean notifyHr;
        private Boolean reminderIfInviteUnopened;
        private Integer reminderAfterDays;

        public NotificationPreferences() {}

        public Boolean getSendInviteEmail() { return sendInviteEmail; }
        public void setSendInviteEmail(Boolean sendInviteEmail) { this.sendInviteEmail = sendInviteEmail; }

        public Boolean getNotifyReportingManager() { return notifyReportingManager; }
        public void setNotifyReportingManager(Boolean notifyReportingManager) { this.notifyReportingManager = notifyReportingManager; }

        public Boolean getNotifyHr() { return notifyHr; }
        public void setNotifyHr(Boolean notifyHr) { this.notifyHr = notifyHr; }

        public Boolean getReminderIfInviteUnopened() { return reminderIfInviteUnopened; }
        public void setReminderIfInviteUnopened(Boolean reminderIfInviteUnopened) { this.reminderIfInviteUnopened = reminderIfInviteUnopened; }

        public Integer getReminderAfterDays() { return reminderAfterDays; }
        public void setReminderAfterDays(Integer reminderAfterDays) { this.reminderAfterDays = reminderAfterDays; }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private final NotificationPreferences np = new NotificationPreferences();
            public Builder sendInviteEmail(Boolean sendInviteEmail) { np.sendInviteEmail = sendInviteEmail; return this; }
            public Builder notifyReportingManager(Boolean notifyReportingManager) { np.notifyReportingManager = notifyReportingManager; return this; }
            public Builder notifyHr(Boolean notifyHr) { np.notifyHr = notifyHr; return this; }
            public Builder reminderIfInviteUnopened(Boolean reminderIfInviteUnopened) { np.reminderIfInviteUnopened = reminderIfInviteUnopened; return this; }
            public Builder reminderAfterDays(Integer reminderAfterDays) { np.reminderAfterDays = reminderAfterDays; return this; }
            public NotificationPreferences build() { return np; }
        }
    }

    public static class AdminUserResponse {
        private String id;
        private String userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String employeeId;
        private String roleId;
        private String departmentId;
        private String designationId;
        private String jobLevelId;
        private String employmentTypeId;
        private String reportingManagerId;
        private String status;
        private String createdAt;
        private WorkInformation workInformation;
        private PersonalInformation personalInformation;
        private IdentityInformation identityInformation;
        private NotificationPreferences notificationPreferences;

        public AdminUserResponse() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }

        public String getDepartmentId() { return departmentId; }
        public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

        public String getDesignationId() { return designationId; }
        public void setDesignationId(String designationId) { this.designationId = designationId; }

        public String getJobLevelId() { return jobLevelId; }
        public void setJobLevelId(String jobLevelId) { this.jobLevelId = jobLevelId; }

        public String getEmploymentTypeId() { return employmentTypeId; }
        public void setEmploymentTypeId(String employmentTypeId) { this.employmentTypeId = employmentTypeId; }

        public String getReportingManagerId() { return reportingManagerId; }
        public void setReportingManagerId(String reportingManagerId) { this.reportingManagerId = reportingManagerId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public WorkInformation getWorkInformation() { return workInformation; }
        public void setWorkInformation(WorkInformation workInformation) { this.workInformation = workInformation; }

        public PersonalInformation getPersonalInformation() { return personalInformation; }
        public void setPersonalInformation(PersonalInformation personalInformation) { this.personalInformation = personalInformation; }

        public IdentityInformation getIdentityInformation() { return identityInformation; }
        public void setIdentityInformation(IdentityInformation identityInformation) { this.identityInformation = identityInformation; }

        public NotificationPreferences getNotificationPreferences() { return notificationPreferences; }
        public void setNotificationPreferences(NotificationPreferences notificationPreferences) { this.notificationPreferences = notificationPreferences; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final AdminUserResponse res = new AdminUserResponse();
            public Builder id(String id) { res.id = id; return this; }
            public Builder userId(String userId) { res.userId = userId; return this; }
            public Builder firstName(String firstName) { res.firstName = firstName; return this; }
            public Builder lastName(String lastName) { res.lastName = lastName; return this; }
            public Builder fullName(String fullName) { res.fullName = fullName; return this; }
            public Builder email(String email) { res.email = email; return this; }
            public Builder employeeId(String employeeId) { res.employeeId = employeeId; return this; }
            public Builder roleId(String roleId) { res.roleId = roleId; return this; }
            public Builder departmentId(String departmentId) { res.departmentId = departmentId; return this; }
            public Builder designationId(String designationId) { res.designationId = designationId; return this; }
            public Builder jobLevelId(String jobLevelId) { res.jobLevelId = jobLevelId; return this; }
            public Builder employmentTypeId(String employmentTypeId) { res.employmentTypeId = employmentTypeId; return this; }
            public Builder reportingManagerId(String reportingManagerId) { res.reportingManagerId = reportingManagerId; return this; }
            public Builder status(String status) { res.status = status; return this; }
            public Builder createdAt(String createdAt) { res.createdAt = createdAt; return this; }
            public Builder workInformation(WorkInformation workInformation) { res.workInformation = workInformation; return this; }
            public Builder personalInformation(PersonalInformation personalInformation) { res.personalInformation = personalInformation; return this; }
            public Builder identityInformation(IdentityInformation identityInformation) { res.identityInformation = identityInformation; return this; }
            public Builder notificationPreferences(NotificationPreferences notificationPreferences) { res.notificationPreferences = notificationPreferences; return this; }
            public AdminUserResponse build() { return res; }
        }
    }
}
