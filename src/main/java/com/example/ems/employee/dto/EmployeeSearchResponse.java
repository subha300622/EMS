package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class EmployeeSearchResponse {
    private List<SearchResultDto> results;
    @Schema(example = "1")
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
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "string")
        private String name;
        @Schema(example = "Software Engineer")
        private String designation;
        @Schema(example = "Engineering")
        private String department;
        @Schema(example = "string")
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
