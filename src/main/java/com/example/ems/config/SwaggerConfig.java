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
                        new Tag().name("Dashboard")
                                .description("Role-based landing dashboards and stats widgets"),
                        new Tag().name("Administration")
                                .description("Administrative actions, roles, permissions, RBAC controls, and announcements"),
                        new Tag().name("Audit Logs")
                                .description("Querying and exporting user activity audit trails"),
                        new Tag().name("Employees")
                                .description("Administrative employee CRUD operations"),
                        new Tag().name("Departments")
                                .description("Administrative department management"),
                        new Tag().name("Directory")
                                .description("Organization directory and manager reporting lines"),
                        new Tag().name("Attendance")
                                .description("Organization-wide attendance tracking and logs"),
                        new Tag().name("Leave Management")
                                .description("Organization-wide leave request and approval tracking"),
                        new Tag().name("Documents")
                                .description("Document management system and permissions"),
                        new Tag().name("Training")
                                .description("Admin training courses and class scheduling"),
                        new Tag().name("Approvals")
                                .description("Unified center for pending leaves, goals, lifecycle, and revision approvals"),
                        new Tag().name("My Profile")
                                .description("Self-service profile retrieval and update endpoints"),
                        new Tag().name("My Assets")
                                .description("Self-service assets registry and support requests"),
                        new Tag().name("My Documents")
                                .description("Self-service personal document repository"),
                        new Tag().name("My Expenses")
                                .description("Self-service expense claim submissions and history"),
                        new Tag().name("My Payslips")
                                .description("Self-service personal payslip retrieval"),
                        new Tag().name("My Performance")
                                .description("Self-service performance reviews and self-assessments"),
                        new Tag().name("My Schedule")
                                .description("Self-service work shift and schedule details"),
                        new Tag().name("My Support")
                                .description("Self-service support and IT helpdesk tickets"),
                        new Tag().name("My Exit")
                                .description("Self-service exit requests and offboarding checklist"),
                        new Tag().name("Payroll Runs")
                                .description("Processing payroll runs and execution logs"),
                        new Tag().name("Payroll Settings")
                                .description("Configuring salary components and tax regime setups"),
                        new Tag().name("Payslips")
                                .description("Administrative payslip generation and distribution"),
                        new Tag().name("Finance")
                                .description("Expense category limits, F&F settlements, and financial analytics"),
                        new Tag().name("Goals")
                                .description("Individual and team goal setups, comments, and progress tracking"),
                        new Tag().name("Performance Reviews")
                                .description("Conducting reviews, performance appraisal cycles, and rating logs"),
                        new Tag().name("Appraisals")
                                .description("Employee appraisal cycles and progress"),
                        new Tag().name("Salary Revisions")
                                .description("Administrative revision workflow, salary history, and increment letters"),
                        new Tag().name("Recruitment")
                                .description("Managing jobs, candidates, interviews, and offer letters"),
                        new Tag().name("Onboarding")
                                .description("Administrative onboarding checklists, tasks, and task assignees"),
                        new Tag().name("Offboarding")
                                .description("Administrative exit procedures and asset clearance tasks"),
                        new Tag().name("Reports")
                                .description("Analytical report generation and system metrics exports"),
                        new Tag().name("Settings")
                                .description("Global settings, password policies, MFA configurations, and system-wide configurations"),
                        new Tag().name("System Monitoring")
                                .description("Health check logs, memory usage, and version info")
                ));
    }
}
