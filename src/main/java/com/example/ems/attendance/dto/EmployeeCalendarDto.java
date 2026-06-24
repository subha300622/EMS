package com.example.ems.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class EmployeeCalendarDto {
    private Long employeeId;
    private String month;
    private List<EmployeeDayRecordDto> calendar;

    public EmployeeCalendarDto(Long employeeId, String month, List<EmployeeDayRecordDto> calendar) {
        this.employeeId = employeeId;
        this.month = month;
        this.calendar = calendar;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public List<EmployeeDayRecordDto> getCalendar() { return calendar; }
    public void setCalendar(List<EmployeeDayRecordDto> calendar) { this.calendar = calendar; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmployeeDayRecordDto {
        private String date;
        private String status;
        private String checkIn;
        private String checkOut;

        public EmployeeDayRecordDto(String date, String status, String checkIn, String checkOut) {
            this.date = date;
            this.status = status;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }

        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    }
}
