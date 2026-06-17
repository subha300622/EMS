package com.example.ems.employee.dto;

import java.util.List;

public class EmployeeSearchResponse {
    private List<SearchResultDto> results;
    private int totalResults;

    public EmployeeSearchResponse() {}

    public EmployeeSearchResponse(List<SearchResultDto> results, int totalResults) {
        this.results = results;
        this.totalResults = totalResults;
    }

    public List<SearchResultDto> getResults() { return results; }
    public void setResults(List<SearchResultDto> results) { this.results = results; }

    public int getTotalResults() { return totalResults; }
    public void setTotalResults(int totalResults) { this.totalResults = totalResults; }

    public static class SearchResultDto {
        private Long employeeId;
        private String name;
        private String designation;
        private String department;
        private String profileImage;

        public SearchResultDto() {}

        public SearchResultDto(Long employeeId, String name, String designation, String department, String profileImage) {
            this.employeeId = employeeId;
            this.name = name;
            this.designation = designation;
            this.department = department;
            this.profileImage = profileImage;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    }
}
