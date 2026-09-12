package com.example.ems.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class EmployeeProfileResponse {
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "EMP101")
    private String employeeCode;
    @Schema(example = "John Doe")
    private String fullName;
    @Schema(example = "string")
    private String profileImage;
    @Schema(example = "Software Engineer")
    private String designation;
    @Schema(example = "Engineering")
    private String department;
    private ManagerProfileDto manager;
    private ContactProfileDto contact;
    private PersonalInfoDto personalInfo;
    private WorkInformationDto workInformation;
    private List<String> skills;

    public EmployeeProfileResponse() {}

    // --- Getters and Setters ---

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public ManagerProfileDto getManager() { return manager; }
    public void setManager(ManagerProfileDto manager) { this.manager = manager; }

    public ContactProfileDto getContact() { return contact; }
    public void setContact(ContactProfileDto contact) { this.contact = contact; }

    public PersonalInfoDto getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfoDto personalInfo) { this.personalInfo = personalInfo; }

    public WorkInformationDto getWorkInformation() { return workInformation; }
    public void setWorkInformation(WorkInformationDto workInformation) { this.workInformation = workInformation; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    // --- Nested DTOs ---

    public static class ManagerProfileDto {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "Jane Smith")
        private String name;

        public ManagerProfileDto() {}

        public ManagerProfileDto(Long employeeId, String name) {
            this.employeeId = employeeId;
            this.name = name;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ContactProfileDto {
        @Schema(example = "john.doe@company.com")
        private String email;
        @Schema(example = "+919876543210")
        private String phone;
        @Schema(example = "Jane Doe - +919876543211")
        private String emergencyContact;

        public ContactProfileDto() {}

        public ContactProfileDto(String email, String phone, String emergencyContact) {
            this.email = email;
            this.phone = phone;
            this.emergencyContact = emergencyContact;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmergencyContact() { return emergencyContact; }
        public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    }

    public static class PersonalInfoDto {
        @Schema(example = "Male")
        private String gender;
        @Schema(example = "1990-05-15")
        private String dateOfBirth;
        @Schema(example = "42, MG Road, Bangalore 560001")
        private String address;

        public PersonalInfoDto() {}

        public PersonalInfoDto(String gender, String dateOfBirth, String address) {
            this.gender = gender;
            this.dateOfBirth = dateOfBirth;
            this.address = address;
        }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class WorkInformationDto {
        @Schema(example = "Bangalore")
        private String location;
        @Schema(example = "HYBRID")
        private String workMode;
        @Schema(example = "2023-01-15")
        private String joiningDate;
        @Schema(example = "FULL_TIME")
        private String employmentType;
        @Schema(example = "ACTIVE")
        private String status;

        public WorkInformationDto() {}

        public WorkInformationDto(String location, String workMode, String joiningDate, String employmentType, String status) {
            this.location = location;
            this.workMode = workMode;
            this.joiningDate = joiningDate;
            this.employmentType = employmentType;
            this.status = status;
        }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }

        public String getJoiningDate() { return joiningDate; }
        public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
