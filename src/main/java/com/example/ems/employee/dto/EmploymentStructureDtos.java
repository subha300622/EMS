package com.example.ems.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class EmploymentStructureDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateEmploymentTypeRequest {
        @NotBlank(message = "employmentType is required")
        @Size(max = 100)
        private String employmentType;

        @Size(max = 500)
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditEmploymentTypeRequest {
        private String employmentTypeId;

        @NotBlank(message = "employmentType is required")
        @Size(max = 100)
        private String employmentType;

        @Size(max = 500)
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmploymentTypeResponse {
        private String employmentTypeId;
        private String employmentType;
        private String description;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateJobLevelRequest {
        @NotBlank(message = "jobLevel is required")
        @Size(max = 100)
        private String jobLevel;

        @Size(max = 500)
        private String description;

        @Valid
        private List<CreateEmploymentTypeRequest> employmentTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditJobLevelRequest {
        private String jobLevelId;

        @NotBlank(message = "jobLevel is required")
        @Size(max = 100)
        private String jobLevel;

        @Size(max = 500)
        private String description;

        @Valid
        private List<EditEmploymentTypeRequest> employmentTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JobLevelResponse {
        private String jobLevelId;
        private String jobLevel;
        private String description;
        private String status;
        private List<EmploymentTypeResponse> employmentTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateEmploymentStructureRequest {
        @NotBlank(message = "designation is required")
        @Size(max = 100)
        private String designation;

        @Size(max = 500)
        private String description;

        @Valid
        private List<CreateJobLevelRequest> jobLevels;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditEmploymentStructureRequest {
        @NotBlank(message = "designation is required")
        @Size(max = 100)
        private String designation;

        @Size(max = 500)
        private String description;

        @Valid
        private List<EditJobLevelRequest> jobLevels;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusRequest {
        @NotBlank(message = "status is required")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmploymentStructureSummaryResponse {
        private String designationId;
        private String designation;
        private String description;
        private String status;
        private List<JobLevelResponse> jobLevels;
        private Integer jobLevelCount;
        private Integer activeJobLevelCount;
        private Integer employmentTypeCount;
        private Integer activeEmploymentTypeCount;
        private Integer userCount;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmploymentStructureResponse {
        private String designationId;
        private String designation;
        private String description;
        private String status;
        private Integer jobLevelCount;
        private Integer activeJobLevelCount;
        private Integer employmentTypeCount;
        private Integer activeEmploymentTypeCount;
        private Integer userCount;
        private List<JobLevelResponse> jobLevels;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmploymentStructurePageResponse {
        private List<EmploymentStructureSummaryResponse> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmploymentStructureDeleteResponse {
        private String message;
        private String designationId;
    }
}
