package com.example.ems.attendance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamCalendarGridDto {
    private String month;
    private Long departmentId;
    private Long managerId;
    private List<CalendarDayDto> calendar;

    public TeamCalendarGridDto(String month, Long departmentId, Long managerId, List<CalendarDayDto> calendar) {
        this.month = month;
        this.departmentId = departmentId;
        this.managerId = managerId;
        this.calendar = calendar;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public List<CalendarDayDto> getCalendar() { return calendar; }
    public void setCalendar(List<CalendarDayDto> calendar) { this.calendar = calendar; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CalendarDayDto {
        private String date;
        private String day;
        private DaySummaryDto summary;
        private List<EmployeeDayDto> employees;

        public CalendarDayDto(String date, String day, DaySummaryDto summary, List<EmployeeDayDto> employees) {
            this.date = date;
            this.day = day;
            this.summary = summary;
            this.employees = employees;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }

        public DaySummaryDto getSummary() { return summary; }
        public void setSummary(DaySummaryDto summary) { this.summary = summary; }

        public List<EmployeeDayDto> getEmployees() { return employees; }
        public void setEmployees(List<EmployeeDayDto> employees) { this.employees = employees; }
    }

    public static class DaySummaryDto {
        private int present;
        private int absent;
        private int onLeave;
        private int holiday;

        public DaySummaryDto(int present, int absent, int onLeave, int holiday) {
            this.present = present;
            this.absent = absent;
            this.onLeave = onLeave;
            this.holiday = holiday;
        }

        public int getPresent() { return present; }
        public void setPresent(int present) { this.present = present; }

        public int getAbsent() { return absent; }
        public void setAbsent(int absent) { this.absent = absent; }

        public int getOnLeave() { return onLeave; }
        public void setOnLeave(int onLeave) { this.onLeave = onLeave; }

        public int getHoliday() { return holiday; }
        public void setHoliday(int holiday) { this.holiday = holiday; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmployeeDayDto {
        private Long employeeId;
        private String name;
        private String status;
        private String checkIn;
        private String checkOut;

        public EmployeeDayDto(Long employeeId, String name, String status, String checkIn, String checkOut) {
            this.employeeId = employeeId;
            this.name = name;
            this.status = status;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }

        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    }
}
