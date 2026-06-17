package com.example.ems.support.dto;

import java.util.List;

public class TicketDetailsResponse {
    private Long ticketId;
    private String ticketNumber;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private CreatedByDto createdBy;
    private AssignedToDto assignedTo;
    private List<AttachmentDto> attachments;
    private SlaDetailDto sla;

    public TicketDetailsResponse() {}

    public TicketDetailsResponse(Long ticketId, String ticketNumber, String subject, String description, String category, String priority, String status, CreatedByDto createdBy, AssignedToDto assignedTo, List<AttachmentDto> attachments, SlaDetailDto sla) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.attachments = attachments;
        this.sla = sla;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CreatedByDto getCreatedBy() { return createdBy; }
    public void setCreatedBy(CreatedByDto createdBy) { this.createdBy = createdBy; }

    public AssignedToDto getAssignedTo() { return assignedTo; }
    public void setAssignedTo(AssignedToDto assignedTo) { this.assignedTo = assignedTo; }

    public List<AttachmentDto> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDto> attachments) { this.attachments = attachments; }

    public SlaDetailDto getSla() { return sla; }
    public void setSla(SlaDetailDto sla) { this.sla = sla; }

    public static class CreatedByDto {
        private Long employeeId;
        private String name;

        public CreatedByDto() {}

        public CreatedByDto(Long employeeId, String name) {
            this.employeeId = employeeId;
            this.name = name;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class AssignedToDto {
        private String team;
        private String agent;

        public AssignedToDto() {}

        public AssignedToDto(String team, String agent) {
            this.team = team;
            this.agent = agent;
        }

        public String getTeam() { return team; }
        public void setTeam(String team) { this.team = team; }

        public String getAgent() { return agent; }
        public void setAgent(String agent) { this.agent = agent; }
    }

    public static class AttachmentDto {
        private String fileId;
        private String fileName;

        public AttachmentDto() {}

        public AttachmentDto(String fileId, String fileName) {
            this.fileId = fileId;
            this.fileName = fileName;
        }

        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }

    public static class SlaDetailDto {
        private String responseDueAt;
        private String resolutionDueAt;
        private String status; // WITHIN_SLA, BREACHED

        public SlaDetailDto() {}

        public SlaDetailDto(String responseDueAt, String resolutionDueAt, String status) {
            this.responseDueAt = responseDueAt;
            this.resolutionDueAt = resolutionDueAt;
            this.status = status;
        }

        public String getResponseDueAt() { return responseDueAt; }
        public void setResponseDueAt(String responseDueAt) { this.responseDueAt = responseDueAt; }

        public String getResolutionDueAt() { return resolutionDueAt; }
        public void setResolutionDueAt(String resolutionDueAt) { this.resolutionDueAt = resolutionDueAt; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
