package com.example.ems.security.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.dto.AuthPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class PermissionCheckService {

    @Autowired
    @Lazy
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Answers: Does the currently authenticated caller have this permission?
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // 1. Super Admin bypass from security context
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String authName = authority.getAuthority();
            if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(authName) || "system.manage".equalsIgnoreCase(authName)) {
                return true;
            }
        }

        // 2. Check via RoleService using authenticated email (dynamic Cache/DB source of truth)
        String email = getAuthenticatedUserEmail();
        if (email != null && roleService != null) {
            Optional<User> userOpt = userRepository.findByWorkEmail(email);
            if (userOpt.isPresent() && userOpt.get().getRole() != null) {
                return roleService.hasPermission(email, permission);
            }
        }

        // 3. Fallback to GrantedAuthorities
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (permission.equalsIgnoreCase(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Enforces that the authenticated caller has the specified permission, throwing AccessDeniedException if not.
     */
    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            publishSecurityAuditDenied(permission, "INSUFFICIENT_PERMISSION: Missing '" + permission + "'");
            throw new AccessDeniedException("Access Denied: Requires '" + permission + "' permission");
        }
    }

    /**
     * Checks if the caller has any of the specified permissions.
     */
    public boolean hasAnyPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(this::hasPermission);
    }

    /**
     * Enforces that the caller has at least one of the specified permissions.
     */
    public void requireAnyPermission(String... permissions) {
        if (!hasAnyPermission(permissions)) {
            String permsStr = Arrays.toString(permissions);
            publishSecurityAuditDenied(permsStr, "INSUFFICIENT_PERMISSION: Missing any of " + permsStr);
            throw new AccessDeniedException("Access Denied: Requires one of " + permsStr + " permissions");
        }
    }

    /**
     * Checks if the target employee matches the authenticated user, or if caller has the required permission.
     */
    public boolean isSelfOrHasPermission(Long employeeId, String permission) {
        if (employeeId != null) {
            Long currentEmployeeId = getCurrentEmployeeId();
            if (employeeId.equals(currentEmployeeId)) {
                return true;
            }
        }
        return hasPermission(permission);
    }

    /**
     * Enforces that caller is either the employee themselves or holds the designated permission.
     */
    public void requireSelfOrPermission(Long employeeId, String permission) {
        if (!isSelfOrHasPermission(employeeId, permission)) {
            publishSecurityAuditDenied(permission, "UNAUTHORIZED_TARGET: Not authorized for employee #" + employeeId);
            throw new AccessDeniedException("Access Denied: You are not authorized to perform this operation on employee #" + employeeId);
        }
    }

    private void publishSecurityAuditDenied(String permission, String failureReason) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publishEvent(com.example.ems.security.event.SecurityAuditEvent.denied(permission, failureReason));
            } catch (Exception e) {
                // Log and don't suppress the subsequent AccessDeniedException
            }
        }
    }

    /**
     * Resolves the email of the currently authenticated principal.
     */
    public String getAuthenticatedUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal.getEmail();
        }
        return null;
    }

    /**
     * Resolves the User entity of the currently authenticated principal.
     */
    public Optional<User> getCurrentUser() {
        String email = getAuthenticatedUserEmail();
        if (email != null) {
            return userRepository.findByWorkEmail(email);
        }
        return Optional.empty();
    }

    /**
     * Resolves the Employee ID corresponding to the currently authenticated user.
     */
    public Long getCurrentEmployeeId() {
        String email = getAuthenticatedUserEmail();
        if (email != null) {
            return employeeRepository.findByEmail(email).map(Employee::getId).orElse(null);
        }
        return null;
    }

    /**
     * Resolves the Employee entity corresponding to the currently authenticated user.
     */
    public Optional<Employee> getCurrentEmployee() {
        String email = getAuthenticatedUserEmail();
        if (email != null) {
            return employeeRepository.findByEmail(email);
        }
        return Optional.empty();
    }
}
