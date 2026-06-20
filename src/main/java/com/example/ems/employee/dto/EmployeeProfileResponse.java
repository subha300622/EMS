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
    private WorkInformationDto workInformation;
    private List<String> skills;

    public EmployeeProfileResponse() {}

    public EmployeeProfileResponse(Long employeeId, String employeeCode, String fullName, String profileImage, String designation, String department, ManagerProfileDto manager, ContactProfileDto contact, WorkInformationDto workInformation, List<String> skills) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.designation = designation;
        this.department = department;
        this.manager = manager;
        this.contact = contact;
        this.workInformation = workInformation;
        this.skills = skills;
    }

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

    public WorkInformationDto getWorkInformation() { return workInformation; }
    public void setWorkInformation(WorkInformationDto workInformation) { this.workInformation = workInformation; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public static class ManagerProfileDto {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "string")
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
        @Schema(example = "john.doe@example.com")
        private String email;
        @Schema(example = "+1-555-0199")
        private String phone;

        public ContactProfileDto() {}

        public ContactProfileDto(String email, String phone) {
            this.email = email;
            this.phone = phone;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class WorkInformationDto {
        @Schema(example = "Bangalore")
        private String location;
        @Schema(example = "string")
        private String workMode;
        @Schema(example = "string")
        private String joiningDate;
        @Schema(example = "ACTIVE")
        private String status;

        public WorkInformationDto() {}

        public WorkInformationDto(String location, String workMode, String joiningDate, String status) {
            this.location = location;
            this.workMode = workMode;
            this.joiningDate = joiningDate;
            this.status = status;
        }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }

        public String getJoiningDate() { return joiningDate; }
        public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
