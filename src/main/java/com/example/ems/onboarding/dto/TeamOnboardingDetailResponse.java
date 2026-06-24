package com.example.ems.onboarding.dto;

import java.time.LocalDate;
import java.util.List;

public class TeamOnboardingDetailResponse {

    private EmployeeInfo employee;
    private BuddyInfo buddy;
    private ProgressInfo progress;
    private List<PhaseInfo> phases;

    public TeamOnboardingDetailResponse() {}

    public TeamOnboardingDetailResponse(EmployeeInfo employee, BuddyInfo buddy, ProgressInfo progress, List<PhaseInfo> phases) {
        this.employee = employee;
        this.buddy = buddy;
        this.progress = progress;
        this.phases = phases;
    }

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public BuddyInfo getBuddy() { return buddy; }
    public void setBuddy(BuddyInfo buddy) { this.buddy = buddy; }

    public ProgressInfo getProgress() { return progress; }
    public void setProgress(ProgressInfo progress) { this.progress = progress; }

    public List<PhaseInfo> getPhases() { return phases; }
    public void setPhases(List<PhaseInfo> phases) { this.phases = phases; }

    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String role;
        private String department;
        private String manager;
        private LocalDate joiningDate;

        public EmployeeInfo() {}

        public EmployeeInfo(Long id, String name, String role, String department, String manager, LocalDate joiningDate) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.department = department;
            this.manager = manager;
            this.joiningDate = joiningDate;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }

        public LocalDate getJoiningDate() { return joiningDate; }
        public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    }

    public static class BuddyInfo {
        private boolean assigned;
        private Long buddyEmployeeId;
        private String name;

        public BuddyInfo() {}

        public BuddyInfo(boolean assigned, Long buddyEmployeeId, String name) {
            this.assigned = assigned;
            this.buddyEmployeeId = buddyEmployeeId;
            this.name = name;
        }

        public boolean isAssigned() { return assigned; }
        public void setAssigned(boolean assigned) { this.assigned = assigned; }

        public Long getBuddyEmployeeId() { return buddyEmployeeId; }
        public void setBuddyEmployeeId(Long buddyEmployeeId) { this.buddyEmployeeId = buddyEmployeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ProgressInfo {
        private int overall;
        private LocalDate expectedCompletionDate;
        private String currentPhase;

        public ProgressInfo() {}

        public ProgressInfo(int overall, LocalDate expectedCompletionDate, String currentPhase) {
            this.overall = overall;
            this.expectedCompletionDate = expectedCompletionDate;
            this.currentPhase = currentPhase;
        }

        public int getOverall() { return overall; }
        public void setOverall(int overall) { this.overall = overall; }

        public LocalDate getExpectedCompletionDate() { return expectedCompletionDate; }
        public void setExpectedCompletionDate(LocalDate expectedCompletionDate) { this.expectedCompletionDate = expectedCompletionDate; }

        public String getCurrentPhase() { return currentPhase; }
        public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }
    }

    public static class PhaseInfo {
        private String name;
        private String status;
        private int completed;
        private int total;

        public PhaseInfo() {}

        public PhaseInfo(String name, String status, int completed, int total) {
            this.name = name;
            this.status = status;
            this.completed = completed;
            this.total = total;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }
}
