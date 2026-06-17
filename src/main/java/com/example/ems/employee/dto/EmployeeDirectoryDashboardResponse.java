package com.example.ems.employee.dto;

public class EmployeeDirectoryDashboardResponse {
    private MyTeamSummary myTeam;
    private DirectorySummary directorySummary;
    private String lastUpdatedAt;

    public EmployeeDirectoryDashboardResponse() {}

    public EmployeeDirectoryDashboardResponse(MyTeamSummary myTeam, DirectorySummary directorySummary, String lastUpdatedAt) {
        this.myTeam = myTeam;
        this.directorySummary = directorySummary;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public MyTeamSummary getMyTeam() { return myTeam; }
    public void setMyTeam(MyTeamSummary myTeam) { this.myTeam = myTeam; }

    public DirectorySummary getDirectorySummary() { return directorySummary; }
    public void setDirectorySummary(DirectorySummary directorySummary) { this.directorySummary = directorySummary; }

    public String getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(String lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public static class MyTeamSummary {
        private String department;
        private String teamName;
        private int totalMembers;

        public MyTeamSummary() {}

        public MyTeamSummary(String department, String teamName, int totalMembers) {
            this.department = department;
            this.teamName = teamName;
            this.totalMembers = totalMembers;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
    }

    public static class DirectorySummary {
        private long totalEmployees;
        private long activeEmployees;
        private long remoteEmployees;
        private long onLeaveEmployees;

        public DirectorySummary() {}

        public DirectorySummary(long totalEmployees, long activeEmployees, long remoteEmployees, long onLeaveEmployees) {
            this.totalEmployees = totalEmployees;
            this.activeEmployees = activeEmployees;
            this.remoteEmployees = remoteEmployees;
            this.onLeaveEmployees = onLeaveEmployees;
        }

        public long getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }

        public long getActiveEmployees() { return activeEmployees; }
        public void setActiveEmployees(long activeEmployees) { this.activeEmployees = activeEmployees; }

        public long getRemoteEmployees() { return remoteEmployees; }
        public void setRemoteEmployees(long remoteEmployees) { this.remoteEmployees = remoteEmployees; }

        public long getOnLeaveEmployees() { return onLeaveEmployees; }
        public void setOnLeaveEmployees(long onLeaveEmployees) { this.onLeaveEmployees = onLeaveEmployees; }
    }
}
