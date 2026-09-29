package com.example.ems.employee.dto.teamleader;

public class TeamLeaderAttendanceTodayResponse {

    private int totalMembers;
    private int present;
    private int remote;
    private int onLeave;
    private int absent;
    private int attendancePercentage;

    public TeamLeaderAttendanceTodayResponse() {}

    public TeamLeaderAttendanceTodayResponse(int totalMembers, int present, int remote, int onLeave, int absent, int attendancePercentage) {
        this.totalMembers = totalMembers;
        this.present = present;
        this.remote = remote;
        this.onLeave = onLeave;
        this.absent = absent;
        this.attendancePercentage = attendancePercentage;
    }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public int getPresent() { return present; }
    public void setPresent(int present) { this.present = present; }

    public int getRemote() { return remote; }
    public void setRemote(int remote) { this.remote = remote; }

    public int getOnLeave() { return onLeave; }
    public void setOnLeave(int onLeave) { this.onLeave = onLeave; }

    public int getAbsent() { return absent; }
    public void setAbsent(int absent) { this.absent = absent; }

    public int getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(int attendancePercentage) { this.attendancePercentage = attendancePercentage; }
}
