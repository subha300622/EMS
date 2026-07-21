package com.example.ems.onboarding.dto;

import java.time.LocalDate;
import java.util.List;

public class OnboardingQueueResponse {

    private List<QueueItem> items;
    private Pagination pagination;

    public List<QueueItem> getItems() { return items; }
    public void setItems(List<QueueItem> items) { this.items = items; }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public static class QueueItem {
        private String id; // format: "onb-001" (derived from DB id)
        private String employeeId; // format: "emp-001" (derived from employee DB id)
        private String initials;
        private String avatarColor;
        private String name;
        private String email;
        private String role;
        private String dept;
        private String deptColor;
        private LocalDate joiningDate;
        private int progress;
        private String status;
        private long daysInOnboarding;
        private String expectedCompletion;
        private String manager;
        private String assignedTemplateId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getInitials() { return initials; }
        public void setInitials(String initials) { this.initials = initials; }

        public String getAvatarColor() { return avatarColor; }
        public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getDept() { return dept; }
        public void setDept(String dept) { this.dept = dept; }

        public String getDeptColor() { return deptColor; }
        public void setDeptColor(String deptColor) { this.deptColor = deptColor; }

        public LocalDate getJoiningDate() { return joiningDate; }
        public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getDaysInOnboarding() { return daysInOnboarding; }
        public void setDaysInOnboarding(long daysInOnboarding) { this.daysInOnboarding = daysInOnboarding; }

        public String getExpectedCompletion() { return expectedCompletion; }
        public void setExpectedCompletion(String expectedCompletion) { this.expectedCompletion = expectedCompletion; }

        public String getManager() { return manager; }
        public void setManager(String manager) { this.manager = manager; }

        public String getAssignedTemplateId() { return assignedTemplateId; }
        public void setAssignedTemplateId(String assignedTemplateId) { this.assignedTemplateId = assignedTemplateId; }
    }

    public static class Pagination {
        private int page;
        private int limit;
        private long total;
        private int totalPages;

        public Pagination() {}

        public Pagination(int page, int limit, long total, int totalPages) {
            this.page = page;
            this.limit = limit;
            this.total = total;
            this.totalPages = totalPages;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
