package com.example.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AdminRoleDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRoleRequest {
        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        private String role;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditRoleRequest {
        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        private String role;

        @Size(max = 500, message = "Description must not exceed 500 characters")
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleStatusRequest {
        @NotBlank(message = "Status is required")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RoleResponse {
        private String roleId;
        private String role;
        private String description;
        private String status;
        private Boolean systemRole;
        private Integer userCount;
        private String createdAt;
        private String updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolePageResponse {
        private List<RoleResponse> content;
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
    public static class RoleDeleteResponse {
        private String message;
        private String roleId;
    }
}
