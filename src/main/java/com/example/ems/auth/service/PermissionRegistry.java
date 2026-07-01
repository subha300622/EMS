package com.example.ems.auth.service;

import java.util.Arrays;
import java.util.List;

public class PermissionRegistry {

        public static final String ATTENDANCE_READ = "attendance.read";
        public static final String ATTENDANCE_MANAGE = "attendance.manage";
        public static final String ATTENDANCE_SELF_READ = "attendance.self.read";
        public static final String EMPLOYEE_ATTENDANCE_READ = "employee.attendance.read";

        public static final List<String> COMMON_SETTINGS_PERMS = Arrays.asList(
                        "settings.self.read", "settings.security.read", "settings.security.update",
                        "settings.privacy.read", "settings.privacy.update", "settings.notifications.read",
                        "settings.notifications.update", "settings.appearance.read", "settings.appearance.update",
                        "settings.language.read", "settings.language.update", "settings.devices.read",
                        "settings.devices.remove", "settings.data.export", "settings.support.create",
                        "settings.support.read");

        public static final List<String> EMPLOYEE_SELF_PERMS = Arrays.asList(
                        "attendance.self.read",
                        "leave.create", "leave.self.read", "payslip.self.read",
                        "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload",
                        "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                        "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read",
                        "asset.self.read",
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
                        "performance.self.goal.update", "performance.self.assessment.submit",
                        "performance.self.feedback.read", "performance.self.history.read",
                        "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                        "schedule.self.notification.read", "schedule.self.timeline.read",
                        "employee.directory.read", "employee.message.create", "employee.contact.read",
                        "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                        "goal.self.update", "goal.submit");

        public static final List<String> MANAGER_PERMS = Arrays.asList(
                        "employee.read", "employee.team.read", "attendance.read", "attendance.team.read",
                        "leave.team.approve",
                        "task.assign", "performance.review",
                        "employee.directory.read", "employee.profile.read", "employee.message.create",
                        "employee.contact.read", "employee.team.hierarchy.read",
                        "goal.create", "goal.read", "goal.update", "goal.self.read", "goal.self.update", "goal.submit",
                        "goal.approve", "goal.reject", "goal.analytics.read",
                        "team.read", "attendance.self.read",
                        "leave.create", "leave.self.read", "payslip.self.read",
                        "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload",
                        "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                        "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read",
                        "asset.self.read",
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
                        "performance.self.goal.update", "performance.self.assessment.submit",
                        "performance.self.feedback.read", "performance.self.history.read",
                        "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                        "schedule.self.notification.read", "schedule.self.timeline.read",
                        "employee.directory.read", "employee.message.create", "employee.contact.read",
                        "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                        "goal.self.update", "goal.submit");

        public static final List<String> FINANCE_PERMS = Arrays.asList(
                        "payroll.read", "payroll.manage", "expense.manage",
                        "salary.manage", "reports.finance",
                        "payslip.read", "employee.payslip.read", "employee.payslip.download",
                        "payslip.self.read", "payslip.self.preview", "payslip.self.download",
                        "fnf.manage", "payroll-settings.manage", "team.read", "audit.read", "audit.export",
                        "attendance.self.read",
                        "leave.create", "leave.self.read", "payslip.self.read",
                        "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload",
                        "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                        "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read",
                        "asset.self.read",
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
                        "performance.self.goal.update", "performance.self.assessment.submit",
                        "performance.self.feedback.read", "performance.self.history.read",
                        "schedule.self.read", "schedule.self.change.create", "schedule.self.availability.update",
                        "schedule.self.notification.read", "schedule.self.timeline.read",
                        "employee.directory.read", "employee.message.create", "employee.contact.read",
                        "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                        "goal.self.update", "goal.submit");

        public static final List<String> HR_PERMS = Arrays.asList(
                        "employee.create", "employee.read", "employee.update",
                        "attendance.read", "leave.approve", "leave.read",
                        "recruitment.manage", "reports.hr",
                        "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.read", "goal.self.update",
                        "goal.submit", "goal.approve", "goal.reject", "goal.analytics.read",
                        "team.read", "asset.manage", "fnf.manage", "announcement.manage", "audit.read", "audit.export");

        public static final List<String> ADMIN_PERMS = Arrays.asList(
                        "user.manage",
                        "user.create", "user.read", "user.update", "user.role.assign",
                        "employee.create", "employee.read", "employee.update",
                        "attendance.manage", "leave.manage", "reports.view",
                        "payroll.read", "payroll.manage", "salary.manage", "payslip.read",
                        "expense.manage",
                        "goal.create", "goal.read", "goal.update", "goal.delete", "goal.approve", "goal.reject",
                        "goal.analytics.read",
                        "performance.review",
                        "role.manage", "permission.manage",
                        "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage", "fnf.manage",
                        "payroll-settings.manage", "announcement.manage");

        public static final List<String> SUPER_ADMIN_PERMS = Arrays.asList(
                        "system.manage", "role.manage", "permission.manage", "user.manage",
                        "user.create", "user.read", "user.update", "user.delete",
                        "employee.create", "employee.read", "employee.update", "employee.delete", "employee.team.read",
                        "attendance.read", "attendance.manage", "attendance.team.read", "attendance.self.read",
                        "leave.create", "leave.read", "leave.approve", "leave.manage", "leave.team.approve",
                        "leave.self.read",
                        "payroll.read", "payroll.manage", "salary.manage", "payslip.read", "payslip.self.read",
                        "reports.view", "reports.hr", "reports.finance", "reports.manager",
                        "recruitment.manage", "task.assign", "performance.review", "expense.manage",
                        "onboarding.self.read", "onboarding.self.update", "onboarding.document.upload",
                        "onboarding.document.read.self", "onboarding.self.submit", "employee.onboarding.read.self",
                        "document.self.read", "expense.self.read", "performance.self.read", "goal.self.read",
                        "asset.self.read",
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
                        "performance.self.goal.update", "performance.self.assessment.submit",
                        "performance.self.feedback.read", "performance.self.history.read",
                        "support.self.create", "support.self.read", "support.self.comment.create", "support.self.close",
                        "goal.create", "goal.read", "goal.update", "goal.delete", "goal.self.update", "goal.submit",
                        "goal.approve", "goal.reject", "goal.analytics.read",
                        "audit.read", "audit.export", "settings.manage", "team.read", "asset.manage", "fnf.manage",
                        "payroll-settings.manage", "announcement.manage");

        public static final List<String> ALL_PERMISSIONS = Arrays.asList(
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
                        "fnf.manage", "payroll-settings.manage", "announcement.manage");
}
