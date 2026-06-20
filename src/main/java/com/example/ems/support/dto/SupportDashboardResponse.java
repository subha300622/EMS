package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class SupportDashboardResponse {
    private EmployeeDto employee;
    private SummaryDto summary;
    private SlaDto sla;
    private List<RecentTicketDto> recentTickets;

    public SupportDashboardResponse() {}

    public SupportDashboardResponse(EmployeeDto employee, SummaryDto summary, SlaDto sla, List<RecentTicketDto> recentTickets) {
        this.employee = employee;
        this.summary = summary;
        this.sla = sla;
        this.recentTickets = recentTickets;
    }

    public EmployeeDto getEmployee() { return employee; }
    public void setEmployee(EmployeeDto employee) { this.employee = employee; }

    public SummaryDto getSummary() { return summary; }
    public void setSummary(SummaryDto summary) { this.summary = summary; }

    public SlaDto getSla() { return sla; }
    public void setSla(SlaDto sla) { this.sla = sla; }

    public List<RecentTicketDto> getRecentTickets() { return recentTickets; }
    public void setRecentTickets(List<RecentTicketDto> recentTickets) { this.recentTickets = recentTickets; }

    public static class EmployeeDto {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "EMP101")
        private String employeeCode;
        @Schema(example = "string")
        private String name;

        public EmployeeDto() {}

        public EmployeeDto(Long employeeId, String employeeCode, String name) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.name = name;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class SummaryDto {
        @Schema(example = "1")
        private long totalTickets;
        @Schema(example = "1")
        private long open;
        @Schema(example = "75")
        private long inProgress;
        @Schema(example = "1")
        private long resolved;
        @Schema(example = "1")
        private long closed;
        @Schema(example = "1")
        private long averageResolutionHours;

        public SummaryDto() {}

        public SummaryDto(long totalTickets, long open, long inProgress, long resolved, long closed, long averageResolutionHours) {
            this.totalTickets = totalTickets;
            this.open = open;
            this.inProgress = inProgress;
            this.resolved = resolved;
            this.closed = closed;
            this.averageResolutionHours = averageResolutionHours;
        }

        public long getTotalTickets() { return totalTickets; }
        public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

        public long getOpen() { return open; }
        public void setOpen(long open) { this.open = open; }

        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }

        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }

        public long getClosed() { return closed; }
        public void setClosed(long closed) { this.closed = closed; }

        public long getAverageResolutionHours() { return averageResolutionHours; }
        public void setAverageResolutionHours(long averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }
    }

    public static class SlaDto {
        @Schema(example = "1")
        private long withinSla;
        @Schema(example = "1")
        private long breached;

        public SlaDto() {}

        public SlaDto(long withinSla, long breached) {
            this.withinSla = withinSla;
            this.breached = breached;
        }

        public long getWithinSla() { return withinSla; }
        public void setWithinSla(long withinSla) { this.withinSla = withinSla; }

        public long getBreached() { return breached; }
        public void setBreached(long breached) { this.breached = breached; }
    }

    public static class RecentTicketDto {
        @Schema(example = "1")
        private Long ticketId;
        @Schema(example = "string")
        private String ticketNumber;
        @Schema(example = "Request for Leave")
        private String subject;
        @Schema(example = "ACTIVE")
        private String status;
        @Schema(example = "string")
        private String priority;
        @Schema(example = "string")
        private String updatedAt;

        public RecentTicketDto() {}

        public RecentTicketDto(Long ticketId, String ticketNumber, String subject, String status, String priority, String updatedAt) {
            this.ticketId = ticketId;
            this.ticketNumber = ticketNumber;
            this.subject = subject;
            this.status = status;
            this.priority = priority;
            this.updatedAt = updatedAt;
        }

        public Long getTicketId() { return ticketId; }
        public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}
