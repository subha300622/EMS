package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class MyTeamResponse {
    private TeamDto team;
    private List<MemberDto> members;
    @Schema(example = "1")
    private int totalMembers;

    public MyTeamResponse() {}

    public MyTeamResponse(TeamDto team, List<MemberDto> members, int totalMembers) {
        this.team = team;
        this.members = members;
        this.totalMembers = totalMembers;
    }

    public TeamDto getTeam() { return team; }
    public void setTeam(TeamDto team) { this.team = team; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public static class TeamDto {
        @Schema(example = "1")
        private Long teamId;
        @Schema(example = "string")
        private String teamName;
        private ManagerDto manager;

        public TeamDto() {}

        public TeamDto(Long teamId, String teamName, ManagerDto manager) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.manager = manager;
        }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public ManagerDto getManager() { return manager; }
        public void setManager(ManagerDto manager) { this.manager = manager; }
    }

    public static class ManagerDto {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "string")
        private String name;
        @Schema(example = "Software Engineer")
        private String designation;

        public ManagerDto() {}

        public ManagerDto(Long employeeId, String name, String designation) {
            this.employeeId = employeeId;
            this.name = name;
            this.designation = designation;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }
    }

    public static class MemberDto {
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
        @Schema(example = "ACTIVE")
        private String employmentStatus;
        @Schema(example = "string")
        private String workMode;
        private ContactDto contact;
        private List<String> skills;

        public MemberDto() {}

        public MemberDto(Long employeeId, String employeeCode, String fullName, String profileImage, String designation, String department, String employmentStatus, String workMode, ContactDto contact, List<String> skills) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.fullName = fullName;
            this.profileImage = profileImage;
            this.designation = designation;
            this.department = department;
            this.employmentStatus = employmentStatus;
            this.workMode = workMode;
            this.contact = contact;
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

        public String getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }

        public ContactDto getContact() { return contact; }
        public void setContact(ContactDto contact) { this.contact = contact; }

        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
    }

    public static class ContactDto {
        @Schema(example = "john.doe@example.com")
        private String email;
        @Schema(example = "+1-555-0199")
        private String phone;

        public ContactDto() {}

        public ContactDto(String email, String phone) {
            this.email = email;
            this.phone = phone;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
