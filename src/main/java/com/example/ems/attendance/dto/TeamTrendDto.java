package com.example.ems.attendance.dto;

import java.util.List;

public class TeamTrendDto {
    private List<String> labels;
    private List<Long> presentCount;
    private List<Long> absentCount;
    private List<Long> lateCount;
    private List<Long> onLeaveCount;

    public TeamTrendDto() {}

    public TeamTrendDto(List<String> labels, List<Long> presentCount, List<Long> absentCount, List<Long> lateCount, List<Long> onLeaveCount) {
        this.labels = labels;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
        this.onLeaveCount = onLeaveCount;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Long> getPresentCount() { return presentCount; }
    public void setPresentCount(List<Long> presentCount) { this.presentCount = presentCount; }

    public List<Long> getAbsentCount() { return absentCount; }
    public void setAbsentCount(List<Long> absentCount) { this.absentCount = absentCount; }

    public List<Long> getLateCount() { return lateCount; }
    public void setLateCount(List<Long> lateCount) { this.lateCount = lateCount; }

    public List<Long> getOnLeaveCount() { return onLeaveCount; }
    public void setOnLeaveCount(List<Long> onLeaveCount) { this.onLeaveCount = onLeaveCount; }
}
