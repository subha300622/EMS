package com.example.ems.auth.service;

import java.util.Arrays;
import java.util.List;

public final class PermissionRegistry {

        private PermissionRegistry() {}

        // Attendance
        public static final String ATTENDANCE_READ = "attendance.read";
        public static final String ATTENDANCE_MANAGE = "attendance.manage";
        public static final String ATTENDANCE_SELF_READ = "attendance.self.read";
        public static final String EMPLOYEE_ATTENDANCE_READ = "employee.attendance.read";

        // Platform Administration
        public static final String PLATFORM_ORGANIZATION_VIEW = "platform.organization.view";
        public static final String PLATFORM_ORGANIZATION_EDIT = "platform.organization.edit";
        public static final String PLATFORM_ROLE_VIEW = "platform.role.view";
        public static final String PLATFORM_ROLE_OVERRIDE = "platform.role.override";
        public static final String PLATFORM_PERMISSION_OVERRIDE = "platform.permission.override";
        public static final String PLATFORM_DASHBOARD_VIEW = "platform.dashboard.view";
        public static final String PLATFORM_AUDIT_VIEW = "platform.audit.view";
        public static final String PLATFORM_REPORTS_VIEW = "platform.reports.view";
        public static final String PLATFORM_DASHBOARD_SUBSCRIPTION_VIEW = "platform.dashboard.subscription.view";
        public static final String PLATFORM_REPORTS_SUBSCRIPTION_VIEW = "platform.reports.subscription.view";
        public static final String PLATFORM_REPORTS_SUBSCRIPTION_EXPORT = "platform.reports.subscription.export";

        // Platform Revenue
        public static final String PLATFORM_REVENUE_DASHBOARD_VIEW = "platform.revenue.dashboard.view";
        public static final String PLATFORM_REVENUE_PAYMENTS_VIEW = "platform.revenue.payments.view";
        public static final String PLATFORM_REVENUE_INVOICES_VIEW = "platform.revenue.invoices.view";
        public static final String PLATFORM_REVENUE_REFUNDS_VIEW = "platform.revenue.refunds.view";
        public static final String PLATFORM_REVENUE_PLANS_VIEW = "platform.revenue.plans.view";
        public static final String PLATFORM_REVENUE_FORECAST_VIEW = "platform.revenue.forecast.view";
        public static final String PLATFORM_REVENUE_EXPORT = "platform.revenue.export";

        // Organization
        public static final String ORGANIZATION_READ = "organization.read";
        public static final String ORGANIZATION_CREATE = "organization.create";
        public static final String ORGANIZATION_UPDATE = "organization.update";
        public static final String ORGANIZATION_DELETE = "organization.delete";
        public static final String ORGANIZATION_SUBSCRIPTION = "organization.subscription";
        public static final String ORGANIZATION_AUDIT_READ = "organization.audit.read";
        public static final String ORGANIZATION_EXPORT = "organization.export";

        // Training Management
        public static final String TRAINING_CREATE = "training.create";
        public static final String TRAINING_READ = "training.read";
        public static final String TRAINING_UPDATE = "training.update";
        public static final String TRAINING_DELETE = "training.delete";
        public static final String TRAINING_APPROVE = "training.approve";
        public static final String TRAINING_PUBLISH = "training.publish";
        public static final String TRAINING_ASSIGN = "training.assign";
        public static final String TRAINING_ATTENDANCE_MANAGE = "training.attendance.manage";
        public static final String TRAINING_LIBRARY_MANAGE = "training.library.manage";
        public static final String TRAINING_REPORTS_VIEW = "training.reports.view";

        public static final List<String> ALL_PERMISSIONS = Arrays.asList(
                        // Training Management
                        "training.create", "training.read", "training.update", "training.delete", "training.approve",
                        "training.publish", "training.assign", "training.attendance.manage", "training.library.manage", "training.reports.view",
                        // User Management
                        "user.create", "user.read", "user.update", "user.delete", "user.manage",
                        // Employee Management
                        "employee.create", "employee.read", "employee.update", "employee.delete", "employee.team.read",
                        // Attendance
                        "attendance.read", "attendance.manage", "attendance.team.read", "attendance.self.read",
                        // Leave Management
                        "leave.create", "leave.read", "leave.approve", "leave.manage", "leave.team.approve",
                        "leave.self.read",
                        // Payroll
                        "payroll.read", "payroll.manage", "salary.manage", "payslip.read", "payslip.self.read",
                        // Reports
                        "reports.view", "reports.hr", "reports.finance", "reports.manager",
                        // System
                        "system.manage", "role.manage", "permission.manage",
                        // Additional
                        "recruitment.manage", "task.assign", "performance.review", "expense.manage",
                        // Onboarding Self-Service
                        "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload",
                        "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                        // New Self Service Permissions
                        "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read",
                        "asset.self.read",
                        // Enterprise Self-Service Permissions
                        "employee.dashboard.read",
                        "employee.profile.read", "employee.profile.update",
                        "employee.onboarding.read", "employee.onboarding.update", "employee.onboarding.document.upload",
                        "employee.onboarding.document.read", "employee.onboarding.submit",
                        "employee.attendance.read", "employee.attendance.create",
                        "employee.leave.create", "employee.leave.read", "employee.leave.cancel",
                        "employee.payslip.read", "employee.payslip.download",
                        "employee.document.read", "employee.document.upload", "employee.document.delete",
                        "employee.asset.read", "employee.asset.request",
                        "employee.expense.create", "employee.expense.read", "employee.expense.update",
                        "employee.performance.read", "employee.performance.self-review.submit",
                        "employee.training.read", "employee.training.complete",
                        "employee.notification.read", "employee.notification.update",
                        "employee.support-ticket.create", "employee.support-ticket.read",
                        "employee.support-ticket.update",
                        "employee.goal.read", "employee.goal.update",
                        "employee.schedule.read",
                        "employee.announcement.read",
                        // My Performance Permissions
                        "performance.self.goal.update", "performance.self.assessment.submit",
                        "performance.self.feedback.read", "performance.self.history.read",
                        "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                        "schedule.self.notification.read", "schedule.self.timeline.read",
                        "employee.directory.read", "employee.message.create", "employee.contact.read",
                        "employee.team.hierarchy.read", "employee.directory.manage", "employee.report.read",
                        // Support Ticket Permissions
                        "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                        "support.view", "support.reply", "support.manage",
                        // Goals Module Permissions
                        "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.update", "goal.submit",
                        "goal.approve", "goal.reject", "goal.analytics.read",
                        // Settings Module Permissions
                        "settings.self.read", "settings.security.read", "settings.security.update",
                        "settings.privacy.read", "settings.privacy.update", "settings.notifications.read",
                        "settings.notifications.update", "settings.appearance.read", "settings.appearance.update",
                        "settings.language.read", "settings.language.update", "settings.devices.read",
                        "settings.devices.remove", "settings.data.export", "settings.support.create",
                        "settings.support.read",
                        // Enterprise Module Permissions
                        "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage",
                        "fnf.manage", "payroll-settings.manage", "announcement.manage",
                        "platform.organization.view", "platform.organization.edit",
                        "platform.role.view", "platform.role.override",
                        "platform.permission.override", "platform.dashboard.view",
                        "platform.audit.view", "platform.reports.view",
                        "platform.dashboard.subscription.view", "platform.reports.subscription.view",
                        "platform.reports.subscription.export",
                        "platform.revenue.dashboard.view", "platform.revenue.payments.view",
                        "platform.revenue.invoices.view", "platform.revenue.refunds.view",
                        "platform.revenue.plans.view", "platform.revenue.forecast.view",
                        "platform.revenue.export",
                        "organization.read", "organization.create", "organization.update", "organization.delete",
                        "organization.subscription", "organization.audit.read", "organization.export");
}
