package com.example.ems.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
                        new Tag().name("Authentication & Security")
                                .description("Manages user authentication, authorization, account security, session management, password recovery, OTP verification, token refresh, invitation acceptance, and secure access to HRMS resources."),
                        new Tag().name("User Management")
                                .description("Manages system user accounts including creation, updates, activation, deactivation, password resets, user search, and account lifecycle administration."),
                        new Tag().name("Role Management")
                                .description("Manages application roles such as HR Administrator, Employee, Manager, Recruiter, Payroll Administrator, and Finance Administrator."),
                        new Tag().name("Permission Management")
                                .description("Manages fine-grained permissions assigned to roles, enabling role-based access control (RBAC) across all HRMS modules."),
                        new Tag().name("Employee Management")
                                .description("Central employee master data repository used to manage employee records, employment details, reporting structure, work information, compensation references, and employee status changes."),
                        new Tag().name("Department Management")
                                .description("Manages organizational departments, department managers, budgets, cost centers, employee allocation, and departmental reporting."),
                        new Tag().name("Organization Directory")
                                .description("Provides employee directory services, reporting hierarchy, organization charts, team structures, manager relationships, and employee lookup capabilities."),
                        new Tag().name("Attendance Management")
                                .description("Manages employee attendance tracking including check-in, check-out, attendance corrections, work hours, attendance reports, payroll summaries, and attendance analytics."),
                        new Tag().name("Leave Management")
                                .description("Handles leave policies, leave types, leave balances, employee leave requests, approvals, cancellations, leave calendars, and leave reporting."),
                        new Tag().name("Payroll Processing")
                                .description("Processes employee payroll, salary calculations, payroll runs, tax deductions, salary disbursement, payroll approvals, and payroll reporting."),
                        new Tag().name("Payslip Management")
                                .description("Generates, stores, exports, downloads, and manages employee payslips and salary statements."),
                        new Tag().name("Salary Management")
                                .description("Manages salary revisions, compensation changes, increment approvals, salary history, and compensation documentation."),
                        new Tag().name("Finance Setup")
                                .description("Configures financial settings including company tax information, bank accounts, payroll settings, payment methods, budgets, and statutory financial parameters."),
                        new Tag().name("Expense Management")
                                .description("Manages employee expense claims, expense categories, reimbursement workflows, receipt management, expense approvals, and expense reporting."),
                        new Tag().name("Finance Onboarding")
                                .description("Manages finance-related onboarding activities including salary setup, payroll activation, tax configuration, statutory compliance verification, and employee banking information."),
                        new Tag().name("Settlement Management")
                                .description("Handles employee Full & Final (F&F) settlements, dues calculation, asset clearance verification, settlement approvals, payment processing, and settlement reporting."),
                        new Tag().name("Asset Management")
                                .description("Manages organizational assets including procurement, assignment, transfers, maintenance, depreciation, lifecycle tracking, returns, disposal, and inventory reporting."),
                        new Tag().name("Document Management")
                                .description("Manages employee and organizational documents including uploads, downloads, approvals, versioning, digital signatures, sharing, retention, and audit tracking."),
                        new Tag().name("Recruitment Management")
                                .description("Manages job postings, candidate applications, interview scheduling, hiring workflows, offer management, candidate tracking, and recruitment analytics."),
                        new Tag().name("Onboarding Management")
                                .description("Manages employee onboarding workflows including onboarding tasks, document collection, training assignments, access provisioning, approvals, and onboarding progress tracking."),
                        new Tag().name("Offboarding Management")
                                .description("Manages employee separation processes including resignations, exit interviews, knowledge transfer, clearance workflows, asset returns, and offboarding activities."),
                        new Tag().name("Performance Management")
                                .description("Manages employee performance reviews, appraisal cycles, manager evaluations, self-assessments, ratings, feedback, and performance improvement plans."),
                        new Tag().name("Goal Management")
                                .description("Manages employee and organizational goals, goal approvals, progress tracking, comments, goal reviews, and performance alignment."),
                        new Tag().name("Training Management")
                                .description("Manages training programs, courses, assessments, certifications, enrollments, learning progress, and employee development initiatives."),
                        new Tag().name("Approval Center")
                                .description("Centralized workflow engine used to review, approve, reject, or send back pending requests across leave, expenses, settlements, onboarding, goals, and HR operations."),
                        new Tag().name("Notification Management")
                                .description("Manages system notifications, employee alerts, announcements, communication broadcasts, and notification tracking."),
                        new Tag().name("Audit & Compliance")
                                .description("Tracks system activities, user actions, business events, data changes, and compliance records for security, governance, and auditing purposes."),
                        new Tag().name("Reports & Analytics")
                                .description("Generates dashboards, KPI metrics, business reports, analytical insights, exports, and organization-wide performance reporting."),
                        new Tag().name("System Administration")
                                .description("Manages platform-wide configuration including company settings, security policies, integrations, monitoring, health checks, metrics, and system maintenance."),
                        new Tag().name("Employee Self Service - Profile")
                                .description("Allows employees to manage their personal profile, dashboard, and account information."),
                        new Tag().name("Employee Self Service - Attendance")
                                .description("Allows employees to check in, check out, and view their attendance history."),
                        new Tag().name("Employee Self Service - Leave")
                                .description("Allows employees to submit leave requests, view leave balances, and track leave history."),
                        new Tag().name("Employee Self Service - Expenses")
                                .description("Allows employees to submit expenses, upload receipts, track approvals, and monitor reimbursements."),
                        new Tag().name("Employee Self Service - Assets")
                                .description("Enables employees to view assigned assets, request assets, report issues, and initiate returns."),
                        new Tag().name("Employee Self Service - Documents")
                                .description("Provides secure access to employee documents, uploads, downloads, document history, and document management."),
                        new Tag().name("Employee Self Service - Payroll")
                                .description("Provides employee access to payslips, salary history, tax documents, and annual payroll statements."),
                        new Tag().name("Employee Self Service - Performance")
                                .description("Enables employees to participate in reviews, submit self-assessments, and track performance progress."),
                        new Tag().name("Employee Self Service - Schedule")
                                .description("Allows employees to manage work schedules, shifts, availability, and schedule change requests."),
                        new Tag().name("Employee Self Service - Exit Management")
                                .description("Supports resignation, exit clearance, asset returns, exit interviews, and final settlement tracking."),
                        new Tag().name("Employee Self Service - Support")
                                .description("Enables employees to create support tickets, access knowledge articles, and communicate with support teams."),
                        new Tag().name("Employee Self Service - Settings")
                                .description("Allows employees to manage account preferences, security settings, privacy controls, notification preferences, and data exports.")
                ));
    }
}
