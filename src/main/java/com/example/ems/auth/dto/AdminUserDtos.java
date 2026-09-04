package com.example.ems.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AdminUserDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkInformation {
        private String locationId;
        private String employeeStatus;
        private String sourceOfHire;
        private String dateOfJoining;
        private Integer totalExperienceYears;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonalInformation {
        private String dateOfBirth;
        private String gender;
        private String maritalStatus;
        private String bloodGroup;
        private String nationality;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IdentityInformation {
        private String aadhaarNumber;
        private String panNumber;
        private String uanNumber;
        private String passportNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationPreferences {
        private Boolean sendInviteEmail;
        private Boolean notifyReportingManager;
        private Boolean notifyHr;
        private Boolean reminderIfInviteUnopened;
        private Integer reminderAfterDays;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
    }
}
