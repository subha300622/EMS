package com.example.ems.employee.dto;

import java.time.LocalDate;
import java.util.List;

public class TeamDtos {

    // 1. Create Request
    public static class TeamCreateRequest {
        private String teamName;
        private String teamCode;
        private String description;
        private Long departmentId;
        private Long teamLeadEmployeeId;

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public String getTeamCode() { return teamCode; }
        public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public Long getTeamLeadEmployeeId() { return teamLeadEmployeeId; }
        public void setTeamLeadEmployeeId(Long teamLeadEmployeeId) { this.teamLeadEmployeeId = teamLeadEmployeeId; }
    }

    // 2. Update Request
    public static class TeamUpdateRequest {
        private String teamName;
        private String teamCode;
        private String description;
        private Long departmentId;
        private Long teamLeadEmployeeId;

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public String getTeamCode() { return teamCode; }
        public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public Long getTeamLeadEmployeeId() { return teamLeadEmployeeId; }
        public void setTeamLeadEmployeeId(Long teamLeadEmployeeId) { this.teamLeadEmployeeId = teamLeadEmployeeId; }
    }

    // 3. Status Update Request
    public static class TeamStatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // 4. Department Update Request
    public static class TeamDepartmentUpdateRequest {
        private Long departmentId;

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    }

    // 5. Team Lead Update Request
    public static class TeamLeadUpdateRequest {
        private Long employeeId;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    }

    // 6. Member Add Request
    public static class TeamMemberAddRequest {
        private Long employeeId;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    }

    // 7. Bulk Member Add Request
    public static class TeamMemberBulkAddRequest {
        private List<Long> employeeIds;

        public List<Long> getEmployeeIds() { return employeeIds; }
        public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }
    }

    // 8. Bulk Member Add Response
    public static class TeamMemberBulkAddResponse {
        private Long teamId;
        private int successCount;
        private int failedCount;
        private List<MemberAddResult> results;

        public TeamMemberBulkAddResponse() {}

        public TeamMemberBulkAddResponse(Long teamId, int successCount, int failedCount, List<MemberAddResult> results) {
            this.teamId = teamId;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.results = results;
        }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

        public List<MemberAddResult> getResults() { return results; }
        public void setResults(List<MemberAddResult> results) { this.results = results; }
    }

    public static class MemberAddResult {
        private Long employeeId;
        private String status;
        private String reason;

        public MemberAddResult() {}

        public MemberAddResult(Long employeeId, String status, String reason) {
            this.employeeId = employeeId;
            this.status = status;
            this.reason = reason;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // 9. Team Response DTO
    public static class TeamResponseDto {
        private Long teamId;
        private String teamName;
        private String teamCode;
        private String description;
        private DepartmentSummary department;
        private TeamLeadSummary teamLead;
        private long memberCount;
        private String status;

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public String getTeamCode() { return teamCode; }
        public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public DepartmentSummary getDepartment() { return department; }
        public void setDepartment(DepartmentSummary department) { this.department = department; }

        public TeamLeadSummary getTeamLead() { return teamLead; }
        public void setTeamLead(TeamLeadSummary teamLead) { this.teamLead = teamLead; }

        public long getMemberCount() { return memberCount; }
        public void setMemberCount(long memberCount) { this.memberCount = memberCount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class DepartmentSummary {
        private Long departmentId;
        private String departmentName;

        public DepartmentSummary() {}

        public DepartmentSummary(Long departmentId, String departmentName) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
        }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    }

    public static class TeamLeadSummary {
        private Long employeeId;
        private String employeeName;

        public TeamLeadSummary() {}

        public TeamLeadSummary(Long employeeId, String employeeName) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    }

    // 10. Team Member List Response DTO
    public static class TeamMemberListResponseDto {
        private Long teamId;
        private String teamName;
        private List<MemberDto> members;

        public TeamMemberListResponseDto() {}

        public TeamMemberListResponseDto(Long teamId, String teamName, List<MemberDto> members) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.members = members;
        }

        public Long getTeamId() { return teamId; }
        public void setTeamId(Long teamId) { this.teamId = teamId; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public List<MemberDto> getMembers() { return members; }
        public void setMembers(List<MemberDto> members) { this.members = members; }
    }

    public static class MemberDto {
        private Long employeeId;
        private String employeeName;
        private String designation;
        private Boolean isTeamLead;
        private LocalDate joinedAt;

        public MemberDto() {}

        public MemberDto(Long employeeId, String employeeName, String designation, Boolean isTeamLead, LocalDate joinedAt) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.designation = designation;
            this.isTeamLead = isTeamLead;
            this.joinedAt = joinedAt;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public Boolean getIsTeamLead() { return isTeamLead; }
        public void setIsTeamLead(Boolean isTeamLead) { this.isTeamLead = isTeamLead; }

        public LocalDate getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDate joinedAt) { this.joinedAt = joinedAt; }
    }
}
