package com.example.ems.common.service;

import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.notification.NotificationRecipientContext;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecipientResolver {

    private static final Logger log = LoggerFactory.getLogger(RecipientResolver.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ApprovalTaskRepository approvalTaskRepository;

    @Autowired
    private RoleService roleService;

    /**
     * Resolves recipients following the strict cascading hierarchy:
     * 1. Explicit approval/assignment user
     * 2. Current workflow step assignee
     * 3. Entity assignee
     * 4. Manager hierarchy
     * 5. Fallback permission holders
     */
    @Transactional(readOnly = true)
    public Set<User> resolveRecipients(NotificationRecipientContext context) {
        if (context == null) {
            return Collections.emptySet();
        }

        Long orgId = context.getOrganizationId();
        if (orgId == null) {
            orgId = TenantContext.getOrganizationId();
        }

        Set<User> recipients = new LinkedHashSet<>();

        // 1. Explicit user
        if (context.getExplicitUserId() != null) {
            resolveExplicitUser(context.getExplicitUserId(), orgId)
                    .ifPresent(recipients::add);
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via explicit user ID: {}", context.getExplicitUserId());
                return recipients;
            }
        }

        // 2. Current workflow step assignee / approver
        if (context.getApprovalTaskId() != null && !context.getApprovalTaskId().isBlank()) {
            recipients.addAll(resolveApprover(context.getApprovalTaskId(), orgId));
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via approval task: {}", context.getApprovalTaskId());
                return recipients;
            }
        }
        if (context.getApproverEmployeeId() != null) {
            resolveEmployeeUser(context.getApproverEmployeeId(), orgId)
                    .ifPresent(recipients::add);
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via approver employee ID: {}", context.getApproverEmployeeId());
                return recipients;
            }
        }

        // 3. Entity assignee
        if (context.getAssigneeUserId() != null) {
            resolveExplicitUser(context.getAssigneeUserId(), orgId)
                    .ifPresent(recipients::add);
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via assignee user ID: {}", context.getAssigneeUserId());
                return recipients;
            }
        }
        if (context.getAssigneeEmployeeId() != null) {
            resolveEmployeeUser(context.getAssigneeEmployeeId(), orgId)
                    .ifPresent(recipients::add);
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via assignee employee ID: {}", context.getAssigneeEmployeeId());
                return recipients;
            }
        }

        // 4. Manager hierarchy
        if (context.getRequesterEmployeeId() != null && context.isResolveManager()) {
            resolveManager(context.getRequesterEmployeeId(), orgId)
                    .ifPresent(recipients::add);
            if (!recipients.isEmpty()) {
                log.debug("Resolved recipient via reporting manager of employee: {}", context.getRequesterEmployeeId());
                return recipients;
            }
        }

        // 5. Fallback: Permission holders within the tenant
        if (context.getRequiredPermission() != null && !context.getRequiredPermission().isBlank()) {
            recipients.addAll(resolvePermissionHolders(context.getRequiredPermission(), orgId));
            log.debug("Resolved {} recipients via fallback permission '{}'", recipients.size(), context.getRequiredPermission());
        }

        return recipients;
    }

    @Transactional(readOnly = true)
    public Optional<User> resolveExplicitUser(Long userId, Long organizationId) {
        if (userId == null) {
            return Optional.empty();
        }
        Optional<User> userOpt;
        if (organizationId != null) {
            userOpt = userRepository.findByIdAndOrganizationId(userId, organizationId);
        } else {
            userOpt = userRepository.findById(userId);
        }
        return userOpt.filter(this::isUserActive);
    }

    @Transactional(readOnly = true)
    public Set<User> resolveApprover(String approvalTaskId, Long organizationId) {
        if (approvalTaskId == null || approvalTaskId.isBlank()) {
            return Collections.emptySet();
        }
        Optional<ApprovalTask> taskOpt = approvalTaskRepository.findByApprovalTaskId(approvalTaskId);
        if (taskOpt.isEmpty()) {
            return Collections.emptySet();
        }
        ApprovalTask task = taskOpt.get();
        Employee approverEmp = task.getApprover();
        if (approverEmp == null) {
            return Collections.emptySet();
        }

        Long targetOrgId = organizationId;
        if (targetOrgId == null && task.getWorkflowInstance() != null && task.getWorkflowInstance().getOrganization() != null) {
            targetOrgId = task.getWorkflowInstance().getOrganization().getId();
        }

        Optional<User> approverUser = resolveEmployeeUser(approverEmp.getId(), targetOrgId);
        return approverUser.map(Collections::singleton).orElse(Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public Optional<User> resolveEmployeeUser(Long employeeId, Long organizationId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null || employee.getEmail() == null) {
            return Optional.empty();
        }

        Long targetOrgId = organizationId;
        if (targetOrgId == null && employee.getOrganization() != null) {
            targetOrgId = employee.getOrganization().getId();
        }

        String email = employee.getEmail().trim().toLowerCase();
        Optional<User> userOpt = Optional.empty();
        if (targetOrgId != null) {
            userOpt = userRepository.findByWorkEmailAndOrganizationId(email, targetOrgId);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByWorkEmail(email);
        }
        return userOpt.filter(this::isUserActive);
    }

    @Transactional(readOnly = true)
    public Optional<User> resolveManager(Long employeeId, Long organizationId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return Optional.empty();
        }

        // Check Employee's direct reporting manager
        Employee manager = employee.getManager();
        if (manager != null) {
            return resolveEmployeeUser(manager.getId(), organizationId);
        }

        // Fallback: Check User reporting manager ID
        Optional<User> userOpt = resolveEmployeeUser(employeeId, organizationId);
        if (userOpt.isPresent() && userOpt.get().getReportingManagerId() != null) {
            return resolveExplicitUser(userOpt.get().getReportingManagerId(), organizationId);
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Set<User> resolvePermissionHolders(String permission, Long organizationId) {
        if (permission == null || permission.isBlank()) {
            return Collections.emptySet();
        }

        List<User> candidates;
        if (organizationId != null) {
            candidates = userRepository.findByOrganizationId(organizationId);
        } else {
            candidates = userRepository.findAll();
        }

        Set<User> matchingUsers = new LinkedHashSet<>();
        for (User user : candidates) {
            if (!isUserActive(user)) {
                continue;
            }
            if (roleService != null && roleService.hasPermission(user.getWorkEmail(), permission)) {
                matchingUsers.add(user);
            }
        }
        return matchingUsers;
    }

    private boolean isUserActive(User user) {
        return user != null && (user.getStatus() == null || "ACTIVE".equalsIgnoreCase(user.getStatus()));
    }
}
