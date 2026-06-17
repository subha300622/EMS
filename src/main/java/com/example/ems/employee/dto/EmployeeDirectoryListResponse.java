package com.example.ems.employee.dto;

import java.util.List;

public class EmployeeDirectoryListResponse {
    private List<EmployeeListItemDto> content;
    private PaginationDto pagination;

    public EmployeeDirectoryListResponse() {}

    public EmployeeDirectoryListResponse(List<EmployeeListItemDto> content, PaginationDto pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<EmployeeListItemDto> getContent() { return content; }
    public void setContent(List<EmployeeListItemDto> content) { this.content = content; }

    public PaginationDto getPagination() { return pagination; }
    public void setPagination(PaginationDto pagination) { this.pagination = pagination; }

    public static class EmployeeListItemDto {
        private Long employeeId;
        private String fullName;
        private String designation;
        private String department;
        private String status;
        private String workMode;
        private String profileImage;
        private List<String> skills;

        public EmployeeListItemDto() {}

        public EmployeeListItemDto(Long employeeId, String fullName, String designation, String department, String status, String workMode, String profileImage, List<String> skills) {
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.designation = designation;
            this.department = department;
            this.status = status;
            this.workMode = workMode;
            this.profileImage = profileImage;
            this.skills = skills;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
    }

    public static class PaginationDto {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;

        public PaginationDto() {}

        public PaginationDto(int page, int size, long totalElements, int totalPages, boolean hasNext) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.hasNext = hasNext;
        }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    }
}
