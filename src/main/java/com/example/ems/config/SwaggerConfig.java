package com.example.ems.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .version("1.0")
                        .description("Enterprise HRMS — Employee Management System REST APIs"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Authentication")
                                .description("Login, registration, password reset, OTP, token refresh & session management"),
                        new Tag().name("User & RBAC")
                                .description("User administration, role management, permissions, and access control"),
                        new Tag().name("Employee Management")
                                .description("Administrative employee CRUD, status updates, and records"),
                        new Tag().name("Organization Management")
                                .description("Department hierarchy, organization directory, manager reporting lines, and teams"),
                        new Tag().name("Attendance Management")
                                .description("Organization-wide attendance tracking, logs, and correction requests"),
                        new Tag().name("Leave Management")
                                .description("Organization-wide leave requests, balances, policies, and calendars"),
                        new Tag().name("Payroll Management")
                                .description("Salary components, tax regimes, payroll runs, payslips, and salary revisions"),
                        new Tag().name("Finance Management")
                                .description("Expense categories, claims, financial analytics, and full & final (F&F) settlements"),
                        new Tag().name("Recruitment")
                                .description("Managing jobs, candidates, interviews, and offer letters"),
                        new Tag().name("Onboarding")
                                .description("New employee onboarding checklists, tasks, and document provisioning"),
                        new Tag().name("Offboarding")
                                .description("Exit procedures, clearance checklists, and asset returns"),
                        new Tag().name("Performance Management")
                                .description("Goals setup, appraisal cycles, rating logs, feedback, and PIPs"),
                        new Tag().name("Asset Management")
                                .description("Asset inventory, assignments, returns, and lifecycle tracking"),
                        new Tag().name("Document Management")
                                .description("Organization-wide document repository, permissions, and sharing"),
                        new Tag().name("Training Management")
                                .description("Training courses, session scheduling, enrollments, and certificates"),
                        new Tag().name("Approval Center")
                                .description("Unified center for pending leaves, goals, and revision approvals"),
                        new Tag().name("Notifications")
                                .description("System-wide notifications, announcements, and read tracking"),
                        new Tag().name("Reports & Analytics")
                                .description("Analytical report generation, dashboards, widgets, and data exports"),
                        new Tag().name("Audit Logs")
                                .description("Querying and exporting user activity audit trails"),
                        new Tag().name("System Administration")
                                .description("Global settings, password policies, health checks, metrics, and version info"),
                        new Tag().name("My Profile")
                                .description("Self-service profile retrieval, update, and basic info"),
                        new Tag().name("My Attendance")
                                .description("Self-service check-in/out, logs, and attendance export"),
                        new Tag().name("My Leave")
                                .description("Self-service leave requests, balances, and history export"),
                        new Tag().name("My Assets")
                                .description("Self-service asset registry and request submissions"),
                        new Tag().name("My Documents")
                                .description("Self-service personal document repository and exports"),
                        new Tag().name("My Expenses")
                                .description("Self-service expense claim submissions and export"),
                        new Tag().name("My Payroll")
                                .description("Self-service payslip retrieval and payslips export"),
                        new Tag().name("My Performance")
                                .description("Self-service performance reviews, self-assessments, goals, and performance export"),
                        new Tag().name("My Schedule")
                                .description("Self-service work shift and schedule details"),
                        new Tag().name("My Exit")
                                .description("Self-service exit clearance request and offboarding checklist"),
                        new Tag().name("My Support")
                                .description("Self-service support ticket submission, details, and discussion"),
                        new Tag().name("My Settings")
                                .description("Self-service account settings, preferences, and personal data export requests")
                ));
    }
}
