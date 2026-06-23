package com.example.ems.attendance.dto;

public class TeamSummaryDto {
    private String date;
    private int totalMembers;
    private int present;
    private int absent;
    private int late;
    private int onLeave;

    public TeamSummaryDto() {}

    public TeamSummaryDto(String date, int totalMembers, int present, int absent, int late, int onLeave) {
        this.date = date;
        this.totalMembers = totalMembers;
        this.present = present;
        this.absent = absent;
        this.late = late;
        this.onLeave = onLeave;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getTotalMembers() { return totalMembers; }
    public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

    public int getPresent() { return present; }
    public void setPresent(int present) { this.present = present; }

    public int getAbsent() { return absent; }
    public void setAbsent(int absent) { this.absent = absent; }

    public int getLate() { return late; }
    public void setLate(int late) { this.late = late; }

    public int getOnLeave() { return onLeave; }
    public void setOnLeave(int onLeave) { this.onLeave = onLeave; }
}
