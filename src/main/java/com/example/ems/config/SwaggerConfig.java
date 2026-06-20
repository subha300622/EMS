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
                                .description("Manages user authentication, authorization, account security, session management, password recovery, OTP verification, token refresh, invitation acceptance, and secure access to HRMS resources. [Business Purpose: Provides secure access control for employees, managers, HR teams, finance teams, and administrators.]"),
                        new Tag().name("User Management")
                                .description("Manages system user accounts including creation, updates, activation, deactivation, password resets, user search, and account lifecycle administration. [Business Purpose: Controls who can access the HRMS platform and how employee accounts are maintained.]"),
                        new Tag().name("Role Management")
                                .description("Manages application roles such as HR Administrator, Employee, Manager, Recruiter, Payroll Administrator, and Finance Administrator. [Business Purpose: Defines user responsibilities and access levels within the organization.]"),
                        new Tag().name("Permission Management")
                                .description("Manages fine-grained permissions assigned to roles, enabling role-based access control (RBAC) across all HRMS modules. [Business Purpose: Ensures users can only access authorized resources and operations.]"),
                        new Tag().name("Employee Management")
                                .description("Central employee master data repository used to manage employee records, employment details, reporting structure, work information, compensation references, and employee status changes. [Business Purpose: Acts as the primary source of truth for all employee information.]"),
                        new Tag().name("Department Management")
                                .description("Manages organizational departments, department managers, budgets, cost centers, employee allocation, and departmental reporting. [Business Purpose: Represents the organization's operational structure.]"),
                        new Tag().name("Organization Directory")
                                .description("Provides employee directory services, reporting hierarchy, organization charts, team structures, manager relationships, and employee lookup capabilities. [Business Purpose: Helps employees understand organizational relationships and reporting structures.]"),
                        new Tag().name("Attendance Management")
                                .description("Manages employee attendance tracking including check-in, check-out, attendance corrections, work hours, attendance reports, payroll summaries, and attendance analytics. [Business Purpose: Tracks employee presence and supports payroll processing.]"),
                        new Tag().name("Leave Management")
                                .description("Handles leave policies, leave types, leave balances, employee leave requests, approvals, cancellations, leave calendars, and leave reporting. [Business Purpose: Supports employee time-off management and workforce planning.]"),
                        new Tag().name("Payroll Processing")
                                .description("Processes employee payroll, salary calculations, payroll runs, tax deductions, salary disbursement, payroll approvals, and payroll reporting. [Business Purpose: Ensures accurate and timely salary payments.]"),
                        new Tag().name("Payslip Management")
                                .description("Generates, stores, exports, downloads, and manages employee payslips and salary statements. [Business Purpose: Provides official salary documentation for employees.]"),
                        new Tag().name("Salary Management")
                                .description("Manages salary revisions, compensation changes, increment approvals, salary history, and compensation documentation. [Business Purpose: Supports compensation planning and employee salary growth.]"),
                        new Tag().name("Finance Setup")
                                .description("Configures financial settings including company tax information, bank accounts, payroll settings, payment methods, budgets, and statutory financial parameters. [Business Purpose: Provides foundational finance and payroll configuration.]"),
                        new Tag().name("Expense Management")
                                .description("Manages employee expense claims, expense categories, reimbursement workflows, receipt management, expense approvals, and expense reporting. [Business Purpose: Controls and tracks employee business expenditures.]"),
                        new Tag().name("Finance Onboarding")
                                .description("Manages finance-related onboarding activities including salary setup, payroll activation, tax configuration, statutory compliance verification, and employee banking information. [Business Purpose: Ensures newly hired employees are payroll-ready.]"),
                        new Tag().name("Settlement Management")
                                .description("Handles employee Full & Final (F&F) settlements, dues calculation, asset clearance verification, settlement approvals, payment processing, and settlement reporting. [Business Purpose: Supports employee exit financial processing.]"),
                        new Tag().name("Asset Management")
                                .description("Manages organizational assets including procurement, assignment, transfers, maintenance, depreciation, lifecycle tracking, returns, disposal, and inventory reporting. [Business Purpose: Ensures effective management of company-owned assets.]"),
                        new Tag().name("Document Management")
                                .description("Manages employee and organizational documents including uploads, downloads, approvals, versioning, digital signatures, sharing, retention, and audit tracking. [Business Purpose: Provides centralized document governance.]"),
                        new Tag().name("Recruitment Management")
                                .description("Manages job postings, candidate applications, interview scheduling, hiring workflows, offer management, candidate tracking, and recruitment analytics. [Business Purpose: Supports talent acquisition and hiring operations.]"),
                        new Tag().name("Onboarding Management")
                                .description("Manages employee onboarding workflows including onboarding tasks, document collection, training assignments, access provisioning, approvals, and onboarding progress tracking. [Business Purpose: Ensures successful employee onboarding experiences.]"),
                        new Tag().name("Offboarding Management")
                                .description("Manages employee separation processes including resignations, exit interviews, knowledge transfer, clearance workflows, asset returns, and offboarding activities. [Business Purpose: Ensures smooth and compliant employee exits.]"),
                        new Tag().name("Performance Management")
                                .description("Manages employee performance reviews, appraisal cycles, manager evaluations, self-assessments, ratings, feedback, and performance improvement plans. [Business Purpose: Supports employee development and performance evaluation.]"),
                        new Tag().name("Goal Management")
                                .description("Manages employee and organizational goals, goal approvals, progress tracking, comments, goal reviews, and performance alignment. [Business Purpose: Aligns employee objectives with organizational strategy.]"),
                        new Tag().name("Training Management")
                                .description("Manages training programs, courses, assessments, certifications, enrollments, learning progress, and employee development initiatives. [Business Purpose: Supports workforce learning and skill development.]"),
                        new Tag().name("Approval Center")
                                .description("Centralized workflow engine used to review, approve, reject, or send back pending requests across leave, expenses, settlements, onboarding, goals, and HR operations. [Business Purpose: Provides a single approval hub for managers and administrators.]"),
                        new Tag().name("Notification Management")
                                .description("Manages system notifications, employee alerts, announcements, communication broadcasts, and notification tracking. [Business Purpose: Ensures timely communication across the organization.]"),
                        new Tag().name("Audit & Compliance")
                                .description("Tracks system activities, user actions, business events, data changes, and compliance records for security, governance, and auditing purposes. [Business Purpose: Provides accountability and regulatory compliance support.]"),
                        new Tag().name("Reports & Analytics")
                                .description("Generates dashboards, KPI metrics, business reports, analytical insights, exports, and organization-wide performance reporting. [Business Purpose: Supports data-driven decision making across HR, Finance, Payroll, and Operations.]"),
                        new Tag().name("System Administration")
                                .description("Manages platform-wide configuration including company settings, security policies, integrations, monitoring, health checks, metrics, and system maintenance. [Business Purpose: Provides centralized administration of the HRMS platform.]"),
                        new Tag().name("Employee Self Service")
                                .description("Provides employees with self-service access to personal information, attendance, leave requests, payroll documents, expenses, assets, performance reviews, schedules, support tickets, and account settings. [Business Purpose: Empowers employees to manage their own HR activities without administrator intervention.]")
                ));
    }
}
