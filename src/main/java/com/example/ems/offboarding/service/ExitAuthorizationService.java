package com.example.ems.offboarding.service;

import com.example.ems.auth.entity.Permission;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.entity.FnfSettlement;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Centralized authorization engine for Employee Exit and F&F Settlement workflows.
 * Evaluates discrete capabilities (VIEW, MANAGE, ACTION, CALCULATE, PAYMENT)
 * against Spring Security granted authorities and domain ownership to prevent
 * BOLA/IDOR, cross-department clearance tampering, and financial authorization bypass.
 */
@Service
public class ExitAuthorizationService {

    // ==========================================
    // 1. EXIT VIEW
    // ==========================================

    public boolean canViewExit(User currentUser, EmployeeExit exit) {
        if (exit == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;
        if (hasAnyAuthority(currentUser, "EXIT_VIEW", "ROLE_HR", "ROLE_ADMIN")) return true;

        // Resigning employee can view their own exit
        if (isUserMatchingEmployee(currentUser, exit.getEmployee())) {
            return true;
        }

        // Direct reporting manager can view their direct report's exit
        if (exit.getReportingManager() != null && isUserMatchingEmployee(currentUser, exit.getReportingManager())) {
            return true;
        }

        return false;
    }

    public void assertCanViewExit(User currentUser, EmployeeExit exit) {
        if (!canViewExit(currentUser, exit)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to view exit request " + (exit != null ? exit.getId() : "null"));
        }
    }

    // ==========================================
    // 2. EXIT MANAGE (HR Offboarding, Reassignment)
    // ==========================================

    public boolean canManageExit(User currentUser, EmployeeExit exit) {
        if (exit == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;

        // Resigning employee cannot manage their own exit lifecycle
        if (isUserMatchingEmployee(currentUser, exit.getEmployee())) {
            return false;
        }

        return hasAnyAuthority(currentUser, "EXIT_MANAGE", "CLEARANCE_MANAGE", "ROLE_HR", "ROLE_ADMIN");
    }

    public void assertCanManageExit(User currentUser, EmployeeExit exit) {
        if (!canManageExit(currentUser, exit)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to manage offboarding for exit " + (exit != null ? exit.getId() : "null"));
        }
    }

    // ==========================================
    // 3. CLEARANCE ACTION (Clear, Hold, Reject)
    // ==========================================

    public boolean canActionClearance(User currentUser, ExitClearance clearance) {
        if (clearance == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;

        // Resigning employee cannot action clearances for their own exit
        if (clearance.getExit() != null && isUserMatchingEmployee(currentUser, clearance.getExit().getEmployee())) {
            return false;
        }

        // HR / Clearance Admin override
        if (hasAnyAuthority(currentUser, "CLEARANCE_MANAGE", "ROLE_HR", "ROLE_ADMIN")) {
            return true;
        }

        // Must be the specifically assigned officer for this clearance task
        if (clearance.getAssignedTo() != null && isUserMatchingEmployee(currentUser, clearance.getAssignedTo())) {
            return true;
        }

        return false;
    }

    public void assertCanActionClearance(User currentUser, ExitClearance clearance) {
        if (!canActionClearance(currentUser, clearance)) {
            String dept = clearance != null ? clearance.getDepartment() : "unknown";
            throw new AccessDeniedException("Access Denied: You are not authorized to action the " + dept + " clearance task.");
        }
    }

    // ==========================================
    // 4. FNF VIEW
    // ==========================================

    public boolean canViewFnf(User currentUser, FnfSettlement settlement) {
        if (settlement == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;
        if (hasAnyAuthority(currentUser, "FNF_VIEW", "ROLE_FINANCE", "ROLE_HR", "ROLE_ADMIN")) return true;

        // Resigning employee can view their own F&F settlement breakdown
        if (settlement.getExit() != null && isUserMatchingEmployee(currentUser, settlement.getExit().getEmployee())) {
            return true;
        }

        return false;
    }

    public void assertCanViewFnf(User currentUser, FnfSettlement settlement) {
        if (!canViewFnf(currentUser, settlement)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to view F&F settlement details.");
        }
    }

    // ==========================================
    // 5. FNF CALCULATE / MODIFY
    // ==========================================

    public boolean canCalculateFnf(User currentUser, EmployeeExit exit) {
        if (exit == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;

        // Resigning employee CANNOT calculate or modify their own F&F settlement!
        if (isUserMatchingEmployee(currentUser, exit.getEmployee())) {
            return false;
        }

        return hasAnyAuthority(currentUser, "FNF_CALCULATE", "ROLE_FINANCE", "ROLE_HR");
    }

    public void assertCanCalculateFnf(User currentUser, EmployeeExit exit) {
        if (!canCalculateFnf(currentUser, exit)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to calculate or modify F&F settlement.");
        }
    }

    // ==========================================
    // 6. FNF PAYMENT RELEASE
    // ==========================================

    public boolean canReleasePayment(User currentUser, FnfSettlement settlement) {
        if (settlement == null) return false;
        if (isPlatformAdmin(currentUser) || isSuperAdmin(currentUser)) return true;

        // Resigning employee CANNOT release payment for themselves!
        if (settlement.getExit() != null && isUserMatchingEmployee(currentUser, settlement.getExit().getEmployee())) {
            return false;
        }

        return hasAnyAuthority(currentUser, "FNF_PAYMENT", "ROLE_FINANCE");
    }

    public void assertCanReleasePayment(User currentUser, FnfSettlement settlement) {
        if (!canReleasePayment(currentUser, settlement)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to release financial disbursements.");
        }
    }

    // ==========================================
    // IDENTITY & AUTHORITY RESOLUTION HELPERS
    // ==========================================

    public boolean isUserMatchingEmployee(User user, Employee employee) {
        if (user == null || employee == null) return false;

        // Compare work email
        if (user.getWorkEmail() != null && employee.getEmail() != null
                && user.getWorkEmail().trim().equalsIgnoreCase(employee.getEmail().trim())) {
            return true;
        }

        // Compare employee code / ID
        if (user.getEmployeeId() != null && employee.getEmployeeId() != null
                && user.getEmployeeId().trim().equalsIgnoreCase(employee.getEmployeeId().trim())) {
            return true;
        }

        // Compare primary numeric ID if mapped
        if (user.getId() != null && employee.getId() != null && user.getId().equals(employee.getId())) {
            return true;
        }

        return false;
    }

    private boolean isPlatformAdmin(User user) {
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            return "PLATFORM_ADMIN".equalsIgnoreCase(roleName) || "ROLE_PLATFORM_ADMIN".equalsIgnoreCase(roleName);
        }
        if (hasAuthority("ROLE_PLATFORM_ADMIN") || hasAuthority("PLATFORM_ADMIN")) return true;
        return false;
    }

    private boolean isSuperAdmin(User user) {
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            return "SUPER_ADMIN".equalsIgnoreCase(roleName) || "ROLE_SUPER_ADMIN".equalsIgnoreCase(roleName);
        }
        if (hasAuthority("ROLE_SUPER_ADMIN") || hasAuthority("SUPER_ADMIN")) return true;
        return false;
    }

    private boolean hasAnyAuthority(User user, String... authorities) {
        Set<String> userAuthorities = collectAllAuthorities(user);
        for (String auth : authorities) {
            String normalized = auth.toUpperCase();
            if (userAuthorities.contains(normalized)) return true;
            if (normalized.startsWith("ROLE_") && userAuthorities.contains(normalized.substring(5))) return true;
            if (!normalized.startsWith("ROLE_") && userAuthorities.contains("ROLE_" + normalized)) return true;
        }
        return false;
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        String normalized = authority.toUpperCase();
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (ga.getAuthority().equalsIgnoreCase(normalized)) return true;
        }
        return false;
    }

    private Set<String> collectAllAuthorities(User user) {
        Set<String> set = new HashSet<>();

        // 1. From User entity role & permissions if present
        if (user != null && user.getRole() != null) {
            Role role = user.getRole();
            if (role.getName() != null) {
                set.add(role.getName().toUpperCase());
                set.add("ROLE_" + role.getName().toUpperCase());
            }
            if (role.getPermissions() != null) {
                for (Permission p : role.getPermissions()) {
                    if (p.getName() != null) {
                        set.add(p.getName().toUpperCase());
                    }
                }
            }
            if (role.getDirectPermissions() != null) {
                for (Permission p : role.getDirectPermissions()) {
                    if (p.getName() != null) {
                        set.add(p.getName().toUpperCase());
                    }
                }
            }
            return set;
        }

        // 2. Fallback to Spring Security context only when user is null or has no role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                set.add(ga.getAuthority().toUpperCase());
            }
        }

        return set;
    }
}
