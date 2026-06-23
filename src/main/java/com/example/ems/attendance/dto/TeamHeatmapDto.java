package com.example.ems.attendance.dto;

import java.util.List;

public class TeamHeatmapDto {
    private String month;
    private List<HeatmapDayDto> data;

    public TeamHeatmapDto(String month, List<HeatmapDayDto> data) {
        this.month = month;
        this.data = data;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public List<HeatmapDayDto> getData() { return data; }
    public void setData(List<HeatmapDayDto> data) { this.data = data; }

    public static class HeatmapDayDto {
        private String date;
        private int presentPercent;
        private String status;

        public HeatmapDayDto(String date, int presentPercent, String status) {
            this.date = date;
            this.presentPercent = presentPercent;
            this.status = status;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getPresentPercent() { return presentPercent; }
        public void setPresentPercent(int presentPercent) { this.presentPercent = presentPercent; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
