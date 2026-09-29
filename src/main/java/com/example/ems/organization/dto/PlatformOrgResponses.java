package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class PlatformOrgResponses {

    @Schema(description = "Organization employee item response")
    public record PlatformOrgEmployeeResponse(
            @Schema(description = "Employee Database ID", example = "1") Long id,
            @Schema(description = "Employee Code", example = "EMP001") String employeeId,
            @Schema(description = "Full Name", example = "John Doe") String fullName,
            @Schema(description = "Work Email", example = "john@example.com") String email,
            @Schema(description = "Department Name", example = "Engineering") String department,
            @Schema(description = "Designation", example = "Software Engineer") String designation,
            @Schema(description = "Status", example = "ACTIVE") String status
    ) {}

    @Schema(description = "Organization admin user item response")
    public record PlatformOrgAdminResponse(
            @Schema(description = "User Database ID", example = "1") Long id,
            @Schema(description = "User Code", example = "USR001") String userId,
            @Schema(description = "Full Name", example = "Jane Doe") String fullName,
            @Schema(description = "Work Email", example = "admin@example.com") String workEmail,
            @Schema(description = "Role Name", example = "ORGANIZATION_ADMIN") String role,
            @Schema(description = "Status", example = "ACTIVE") String status
    ) {}

    @Schema(description = "Organization audit log item response")
    public record PlatformOrgAuditLogResponse(
            @Schema(description = "Audit Log ID", example = "1") Long id,
            @Schema(description = "Action Performed", example = "UPDATE_SUBSCRIPTION") String action,
            @Schema(description = "Target Entity", example = "ORGANIZATION") String entity,
            @Schema(description = "Target Entity ID", example = "10") Long entityId,
            @Schema(description = "Performed By Email", example = "admin@example.com") String performedBy,
            @Schema(description = "Performed Timestamp", example = "2026-07-01T10:00:00Z") String performedAt,
            @Schema(description = "Old Values JSON", example = "{}") String oldValues,
            @Schema(description = "New Values JSON", example = "{}") String newValues
    ) {}

    @Schema(description = "Global dashboard search results response")
    public record PlatformGlobalSearchResponse(
            @Schema(description = "Matching Organizations") List<OrgSearchItem> organizations,
            @Schema(description = "Matching Employees") List<EmpSearchItem> employees,
            @Schema(description = "Matching Departments") List<DeptSearchItem> departments,
            @Schema(description = "Matching Users") List<UserSearchItem> users
    ) {
        @Schema(description = "Organization search result item")
        public record OrgSearchItem(
                @Schema(description = "Organization ID", example = "1") Long id,
                @Schema(description = "Organization Code", example = "ORG-1001") String organizationCode,
                @Schema(description = "Organization Name", example = "Acme Corp") String name,
                @Schema(description = "Email", example = "contact@acme.com") String email,
                @Schema(description = "Status", example = "ACTIVE") String status
        ) {}

        @Schema(description = "Employee search result item")
        public record EmpSearchItem(
                @Schema(description = "Employee ID", example = "1") Long id,
                @Schema(description = "Employee Code", example = "EMP001") String employeeId,
                @Schema(description = "Full Name", example = "John Doe") String fullName,
                @Schema(description = "Email", example = "john@example.com") String email,
                @Schema(description = "Department", example = "Engineering") String department,
                @Schema(description = "Designation", example = "Software Engineer") String designation
        ) {}

        @Schema(description = "Department search result item")
        public record DeptSearchItem(
                @Schema(description = "Department ID", example = "1") Long id,
                @Schema(description = "Department Code", example = "ENG") String code,
                @Schema(description = "Department Name", example = "Engineering") String name,
                @Schema(description = "Status", example = "ACTIVE") String status
        ) {}

        @Schema(description = "User search result item")
        public record UserSearchItem(
                @Schema(description = "User ID", example = "1") Long id,
                @Schema(description = "User Code", example = "USR001") String userId,
                @Schema(description = "Full Name", example = "Jane Doe") String fullName,
                @Schema(description = "Work Email", example = "jane@example.com") String workEmail,
                @Schema(description = "Role", example = "HR_MANAGER") String role
        ) {}
    }
}
