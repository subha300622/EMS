package com.example.ems.offboarding.dto;

import java.util.List;

public class ExitKtPlanResponse {
    private Long employeeId;
    private String employeeName;
    private String role;
    private List<ProjectDto> projects;
    private List<ContactDto> keyContacts;
    private List<SystemCredentialDto> systemCredentials;
    private List<TaskDto> pendingTasks;
    private HandoverPersonDto handoverPerson;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<ProjectDto> getProjects() { return projects; }
    public void setProjects(List<ProjectDto> projects) { this.projects = projects; }

    public List<ContactDto> getKeyContacts() { return keyContacts; }
    public void setKeyContacts(List<ContactDto> keyContacts) { this.keyContacts = keyContacts; }

    public List<SystemCredentialDto> getSystemCredentials() { return systemCredentials; }
    public void setSystemCredentials(List<SystemCredentialDto> systemCredentials) { this.systemCredentials = systemCredentials; }

    public List<TaskDto> getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(List<TaskDto> pendingTasks) { this.pendingTasks = pendingTasks; }

    public HandoverPersonDto getHandoverPerson() { return handoverPerson; }
    public void setHandoverPerson(HandoverPersonDto handoverPerson) { this.handoverPerson = handoverPerson; }

    // Nested DTOs
    public static class ProjectDto {
        private Long projectId;
        private String projectName;
        private String status;
        private String handoverNotes;
        private String riskLevel;

        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getHandoverNotes() { return handoverNotes; }
        public void setHandoverNotes(String handoverNotes) { this.handoverNotes = handoverNotes; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class ContactDto {
        private Long contactId;
        private String name;
        private String role;
        private String email;
        private String responsibility;

        public Long getContactId() { return contactId; }
        public void setContactId(Long contactId) { this.contactId = contactId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getResponsibility() { return responsibility; }
        public void setResponsibility(String responsibility) { this.responsibility = responsibility; }
    }

    public static class SystemCredentialDto {
        private Long systemId;
        private String systemName;
        private String accessType;
        private String status;
        private String handoverStatus;

        public Long getSystemId() { return systemId; }
        public void setSystemId(Long systemId) { this.systemId = systemId; }

        public String getSystemName() { return systemName; }
        public void setSystemName(String systemName) { this.systemName = systemName; }

        public String getAccessType() { return accessType; }
        public void setAccessType(String accessType) { this.accessType = accessType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getHandoverStatus() { return handoverStatus; }
        public void setHandoverStatus(String handoverStatus) { this.handoverStatus = handoverStatus; }
    }

    public static class TaskDto {
        private Long taskId;
        private String taskName;
        private String status;
        private String dueDate;

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }

        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    }

    public static class HandoverPersonDto {
        private Long employeeId;
        private String name;
        private String role;

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
