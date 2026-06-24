package com.example.ems.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Enterprise HRMS API",
        version = "1.0",
        description = """
        Comprehensive Human Resource Management System (HRMS) APIs supporting
        employee lifecycle management, recruitment, onboarding, attendance,
        leave, payroll, finance, performance, training, assets, documents,
        reporting, approvals, and employee self-service operations.
        """
    )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Enterprise HRMS API")
                        .version("1.0")
                        .description("Comprehensive Human Resource Management System (HRMS) APIs supporting employee lifecycle management, recruitment, onboarding, attendance, leave, payroll, finance, performance, training, assets, documents, reporting, approvals, and employee self-service operations."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Employees").description("Employee records, employment detail, reporting lines, work information."),
                        new Tag().name("Department Management").description("Departments, department managers, budgets, and departmental structure."),
                        new Tag().name("Organization Directory").description("Employee directory, organization charts, lookup and reporting hierarchies."),
                        new Tag().name("Employee Directory").description("Corporate employee directory and contact search."),
                        new Tag().name("User Management").description("System user accounts, lifecycle administration, user search."),
                        new Tag().name("Role Management").description("Role configurations (HR, Employee, Manager, Recruiter, Finance, Admin)."),
                        new Tag().name("Attendance Management").description("Clock-in/out swipe logs, attendance summaries, punch override requests."),
                        new Tag().name("Attendance Logs Management").description("System attendance logs and punch history."),
                        new Tag().name("Attendance Regularization Management").description("Attendance corrections, punch adjustment approvals, manager review notes."),
                        new Tag().name("Team Attendance Analytics").description("Pivoted team monthly views, calendars, heatmaps, and coverage analytics."),
                        new Tag().name("Team Scheduling Module").description("Manager team schedule overview grid, shift assignments, and painter operations."),
                        new Tag().name("Leave Management").description("Leave requests submission, approvals, leave balances, and holidays."),
                        new Tag().name("Payroll Processing").description("Processes payroll cycles, salary calculations, deductions, and payroll runs."),
                        new Tag().name("Payslip Management").description("Generates, exports, downloads, and manages employee salary slips."),
                        new Tag().name("Salary Management").description("Salary revisions, compensation changes, increment approvals, salary history."),
                        new Tag().name("Expense Management").description("Employee expense claims, category setup, and reimbursement approvals."),
                        new Tag().name("Finance Setup").description("Configures tax details, company bank accounts, payment methods, and budgets."),
                        new Tag().name("Finance Onboarding").description("Onboarding salary setups, payroll accounts activation, statutory compliance."),
                        new Tag().name("Settlement Management").description("Employee Full & Final (F&F) settlements, exit dues computation."),
                        new Tag().name("Performance Reviews").description("Annual appraisals, performance cycles, evaluations, feedback logs."),
                        new Tag().name("Performance Cycles").description("Performance review cycle configuration and deadlines."),
                        new Tag().name("Increment Policies").description("Salary increment guidelines and policies based on appraisal ratings."),
                        new Tag().name("Goal Management").description("Employee key goals management, progress updates, alignment settings."),
                        new Tag().name("Appraisals").description("Review loops, manager-level ratings, performance documentation."),
                        new Tag().name("Recruitment Management").description("Job openings, applications, interview tracking, candidate pipeline, offer letters."),
                        new Tag().name("Asset Management").description("Organization hardware assets assignments, return logs, value depreciation."),
                        new Tag().name("Document Management").description("Employee secure document folder, uploads, downloads, history verification."),
                        new Tag().name("Onboarding Management").description("Hiring task checklists, statutory forms collection, access provisioning."),
                        new Tag().name("Offboarding Management").description("Resignations, exit clearances, asset returns, exit interviews."),
                        new Tag().name("Training Management").description("Learning courses, assessments, enrollments, learning progress."),
                        new Tag().name("Notification Management").description("System notification templates, logs, and user notifications."),
                        new Tag().name("Authentication & Security").description("Bearer authentication, JWT session security, token refreshes."),
                        new Tag().name("Permission Management").description("Application permissions list management."),
                        new Tag().name("Audit & Compliance").description("System activity logging, event tracking, auditing history."),
                        new Tag().name("System Administration").description("Company security rules, notifications settings, UI preferences, system monitoring."),
                        new Tag().name("Webhook Integration").description("Webhooks configuration and outgoing event dispatch logs."),
                        new Tag().name("Approval Center").description("Pending approvals list, actions history, and workflow routing."),
                        new Tag().name("Reports & Analytics").description("Payroll reports, attendance logs, finance reports, export files."),
                        new Tag().name("HR Dashboard").description("HR metrics, team composition, department distribution KPIs."),
                        new Tag().name("Manager Dashboard").description("Manager team overview, pending approvals, and operational metrics."),
                        new Tag().name("Employee Self Service - Profile").description("Self-Service profile updates, address details, phone number revisions."),
                        new Tag().name("Employee Self Service - Attendance").description("Self-Service check-in, check-out, personal monthly attendance views."),
                        new Tag().name("Employee Self Service - Schedule").description("Self-Service calendar dashboard, shift changes requests."),
                        new Tag().name("Employee Self Service - Payroll").description("Self-Service payslip downloads, compensation slips history."),
                        new Tag().name("Employee Self Service - Expenses").description("Self-Service reimbursement logs, receipt uploads, request progress."),
                        new Tag().name("Employee Self Service - Documents").description("Self-Service personal document safe uploads, downloads."),
                        new Tag().name("Employee Self Service - Performance").description("Self-Service reviews, ratings logs, assessments submissions."),
                        new Tag().name("Employee Self Service - Exit Management").description("Self-Service resignation submissions, final clearance tracking."),
                        new Tag().name("Employee Self Service - Settings").description("Self-Service user preferences, MFA setup, data export."),
                        new Tag().name("Employee Self Service - Support").description("Self-Service help desk tickets submission, comments, ticketing lifecycle management."),
                        new Tag().name("Employee Self Service - Assets").description("Self-Service assigned assets listing, damage reports, return requests.")
                ));
    }

    private OpenApiCustomizer filterByTagsCustomizer(Set<String> allowedTags, List<String> excludes, boolean isESS) {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            Set<String> usedTagNames = new HashSet<>();
            List<String> pathsToRemove = new java.util.ArrayList<>();

            openApi.getPaths().forEach((path, pathItem) -> {
                // Check if path is excluded
                if (excludes != null) {
                    for (String ex : excludes) {
                        if (path.contains(ex)) {
                            pathsToRemove.add(path);
                            return;
                        }
                    }
                }

                // Filter operations
                if (pathItem.getGet() != null) {
                    if (hasAllowedTag(pathItem.getGet(), allowedTags, isESS)) {
                        if (pathItem.getGet().getTags() != null) {
                            usedTagNames.addAll(pathItem.getGet().getTags());
                        }
                    } else {
                        pathItem.setGet(null);
                    }
                }
                if (pathItem.getPost() != null) {
                    if (hasAllowedTag(pathItem.getPost(), allowedTags, isESS)) {
                        if (pathItem.getPost().getTags() != null) {
                            usedTagNames.addAll(pathItem.getPost().getTags());
                        }
                    } else {
                        pathItem.setPost(null);
                    }
                }
                if (pathItem.getPut() != null) {
                    if (hasAllowedTag(pathItem.getPut(), allowedTags, isESS)) {
                        if (pathItem.getPut().getTags() != null) {
                            usedTagNames.addAll(pathItem.getPut().getTags());
                        }
                    } else {
                        pathItem.setPut(null);
                    }
                }
                if (pathItem.getDelete() != null) {
                    if (hasAllowedTag(pathItem.getDelete(), allowedTags, isESS)) {
                        if (pathItem.getDelete().getTags() != null) {
                            usedTagNames.addAll(pathItem.getDelete().getTags());
                        }
                    } else {
                        pathItem.setDelete(null);
                    }
                }
                if (pathItem.getPatch() != null) {
                    if (hasAllowedTag(pathItem.getPatch(), allowedTags, isESS)) {
                        if (pathItem.getPatch().getTags() != null) {
                            usedTagNames.addAll(pathItem.getPatch().getTags());
                        }
                    } else {
                        pathItem.setPatch(null);
                    }
                }
                if (pathItem.getOptions() != null) {
                    if (hasAllowedTag(pathItem.getOptions(), allowedTags, isESS)) {
                        if (pathItem.getOptions().getTags() != null) {
                            usedTagNames.addAll(pathItem.getOptions().getTags());
                        }
                    } else {
                        pathItem.setOptions(null);
                    }
                }
                if (pathItem.getHead() != null) {
                    if (hasAllowedTag(pathItem.getHead(), allowedTags, isESS)) {
                        if (pathItem.getHead().getTags() != null) {
                            usedTagNames.addAll(pathItem.getHead().getTags());
                        }
                    } else {
                        pathItem.setHead(null);
                    }
                }

                // Check if the pathItem has no operations left
                if (pathItem.readOperations() == null || pathItem.readOperations().isEmpty()) {
                    pathsToRemove.add(path);
                }
            });

            pathsToRemove.forEach(path -> openApi.getPaths().remove(path));

            // Filter the OpenAPI tags list to only contain the allowed tags that are actually used
            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .filter(tag -> usedTagNames.contains(tag.getName()))
                        .collect(Collectors.toList()));
            }
        };
    }

    private boolean hasAllowedTag(io.swagger.v3.oas.models.Operation operation, Set<String> allowedTags, boolean isESS) {
        if (operation.getTags() == null) {
            return false;
        }
        if (isESS) {
            return operation.getTags().stream().anyMatch(tag -> tag.startsWith("Employee Self Service - "));
        }
        return operation.getTags().stream().anyMatch(allowedTags::contains);
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("All APIs")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi hrCoreApi() {
        return GroupedOpenApi.builder()
                .group("HR Core & Directory")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Employees",
                        "Department Management",
                        "Organization Directory",
                        "Employee Directory"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi attendanceApi() {
        return GroupedOpenApi.builder()
                .group("Attendance & Scheduling")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Attendance Management",
                        "Attendance Logs Management",
                        "Attendance Regularization Management",
                        "Team Attendance Analytics",
                        "Team Scheduling Module",
                        "Leave Management"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi payrollApi() {
        return GroupedOpenApi.builder()
                .group("Payroll & Finance")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Payroll Processing",
                        "Payslip Management",
                        "Salary Management",
                        "Finance Setup",
                        "Finance Onboarding",
                        "Settlement Management",
                        "Expense Management"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi performanceCoreApi() {
        return GroupedOpenApi.builder()
                .group("Performance Core")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Performance Reviews",
                        "Performance Cycles",
                        "Increment Policies",
                        "Goal Management",
                        "Appraisals"
                ), List.of("/reports/", "/dashboard/", "/analytics/"), false))
                .build();
    }

    @Bean
    public GroupedOpenApi performanceAnalyticsApi() {
        return GroupedOpenApi.builder()
                .group("Performance Analytics")
                .pathsToMatch(
                        "/api/v1/performance/**/reports/**",
                        "/api/v1/performance/**/dashboard/**",
                        "/api/v1/performance/**/analytics/**"
                )
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Performance Reviews",
                        "Performance Cycles",
                        "Increment Policies",
                        "Goal Management",
                        "Appraisals"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi recruitmentApi() {
        return GroupedOpenApi.builder()
                .group("Recruitment")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Recruitment Management"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi operationsApi() {
        return GroupedOpenApi.builder()
                .group("Operations")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Asset Management",
                        "Document Management",
                        "Onboarding Management",
                        "Offboarding Management",
                        "Training Management",
                        "Notification Management"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi securityAdminApi() {
        return GroupedOpenApi.builder()
                .group("Security & Administration")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Authentication & Security",
                        "Audit & Compliance",
                        "Permission Management",
                        "Role Management",
                        "User Management",
                        "System Administration",
                        "Webhook Integration",
                        "Approval Center"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi analyticsReportsApi() {
        return GroupedOpenApi.builder()
                .group("Analytics & Reports")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(Set.of(
                        "Reports & Analytics",
                        "HR Dashboard",
                        "Manager Dashboard"
                ), null, false))
                .build();
    }

    @Bean
    public GroupedOpenApi essApi() {
        return GroupedOpenApi.builder()
                .group("Employee Self Service")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(filterByTagsCustomizer(null, null, true))
                .build();
    }
}
