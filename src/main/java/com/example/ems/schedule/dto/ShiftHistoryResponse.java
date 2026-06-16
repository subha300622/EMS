package com.example.ems.schedule.dto;

import java.util.List;

public class ShiftHistoryResponse {

    private List<ShiftItem> shifts;

    public ShiftHistoryResponse() {}

    public ShiftHistoryResponse(List<ShiftItem> shifts) {
        this.shifts = shifts;
    }

    public static class ShiftItem {
        private Long shiftId;
        private String shiftName;
        private String date;
        private String startTime;
        private String endTime;
        private Integer durationHours;
        private String location;
        private String status;

        // Getters and Setters
        public Long getShiftId() { return shiftId; }
        public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
        public String getShiftName() { return shiftName; }
        public void setShiftName(String shiftName) { this.shiftName = shiftName; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public Integer getDurationHours() { return durationHours; }
        public void setDurationHours(Integer durationHours) { this.durationHours = durationHours; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public List<ShiftItem> getShifts() { return shifts; }
    public void setShifts(List<ShiftItem> shifts) { this.shifts = shifts; }
}
