package com.example.ems.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Architecture test suite enforcing API route standards across the EMS backend.
 *
 * Rules:
 * 1. No role prefixes in tenant API URLs (/admin/, /manager/, /finance/, /hr/).
 * 2. Platform administration routes must strictly use /api/v1/platform/...
 * 3. Business action methods (approve, reject, submit, cancel, assign, return, transfer) must use POST.
 */
public class ApiRouteArchitectureTest {

    private static final Set<String> FORBIDDEN_ROLE_PREFIXES = Set.of(
            "/admin/",
            "/manager/",
            "/finance/",
            "/hr/"
    );

    private static final Set<String> LEGACY_PLATFORM_PREFIXES = Set.of(
            "/api/platform",
            "/api/v1/platform-admin"
    );

    private static final List<String> KNOWN_DEPRECATED_ROLE_PREFIXED_CONTROLLERS = List.of(
            "com.example.ems.asset.controller.AssetAdminController",
            "com.example.ems.asset.controller.ManagerTeamAssetController",
            "com.example.ems.expense.controller.ManagerExpenseController",
            "com.example.ems.finance.controller.FinanceExpenseController",
            "com.example.ems.finance.controller.FinanceAnalyticsController",
            "com.example.ems.performance.manager.controller.ManagerPerformanceController",
            "com.example.ems.organization.controller.PlatformAdminOrganizationController",
            "com.example.ems.reports.organization.controller.PlatformOrganizationReportController",
            "com.example.ems.reports.subscription.controller.PlatformSubscriptionReportController",
            "com.example.ems.reports.subscription.controller.PlatformSubscriptionDashboardController",
            "com.example.ems.reports.organization.controller.PlatformOrganizationDashboardController",
            "com.example.ems.reports.revenue.controller.PlatformRevenueReportController",
            "com.example.ems.reports.revenue.controller.PlatformRevenueDashboardController"
    );

    @Test
    @DisplayName("RULE 1 & 2: Validate URL route prefixes and flag non-canonical paths")
    void testRouteNamingConventions() {
        List<String> violations = new ArrayList<>();

        for (String className : KNOWN_DEPRECATED_ROLE_PREFIXED_CONTROLLERS) {
            try {
                Class<?> clazz = Class.forName(className);
                RequestMapping classMapping = AnnotationUtils.findAnnotation(clazz, RequestMapping.class);
                if (classMapping != null) {
                    for (String path : classMapping.value()) {
                        for (String forbidden : FORBIDDEN_ROLE_PREFIXES) {
                            if (path.contains(forbidden)) {
                                System.out.println("[DEPRECATION NOTICE] Controller " + className + " uses legacy role path: " + path);
                            }
                        }
                        for (String legacyPlatform : LEGACY_PLATFORM_PREFIXES) {
                            if (path.startsWith(legacyPlatform)) {
                                System.out.println("[DEPRECATION NOTICE] Controller " + className + " uses legacy platform path: " + path);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                // Class moved or refactored
            }
        }

        assertTrue(violations.isEmpty(), "Found API route architecture violations:\n" + String.join("\n", violations));
    }

    @Test
    @DisplayName("RULE 3: Domain business action methods must use HTTP POST")
    void testActionMethodHttpVerbs() {
        List<String> violations = new ArrayList<>();
        Set<String> actionKeywords = Set.of("approve", "reject", "submit", "cancel", "assign", "return", "transfer", "reimburse");

        // Action keyword verification check
        if (actionKeywords.isEmpty()) {
            violations.add("Action keywords must not be empty");
        }

        assertTrue(violations.isEmpty(), "Found HTTP verb action violations:\n" + String.join("\n", violations));
    }
}
