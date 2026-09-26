package com.example.ems.employee.dto.teamleader;

public class TeamMemberRosterDto {

    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private String department;
    private String designation;
    private String status;
    private String attendanceStatus;
    private boolean isTeamLead;

    public TeamMemberRosterDto() {}

    public TeamMemberRosterDto(Long id, String employeeId, String name, String email, String department, String designation, String status, String attendanceStatus, boolean isTeamLead) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.designation = designation;
        this.status = status;
        this.attendanceStatus = attendanceStatus;
        this.isTeamLead = isTeamLead;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public boolean isTeamLead() { return isTeamLead; }
    public void setTeamLead(boolean teamLead) { isTeamLead = teamLead; }
}
