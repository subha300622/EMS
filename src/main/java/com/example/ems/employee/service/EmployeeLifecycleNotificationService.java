package com.example.ems.employee.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.repository.NotificationRepository;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service responsible for dispatching communication-only lifecycle notifications
 * using deduplicated idempotency keys.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class EmployeeLifecycleNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeLifecycleNotificationService.class);
    private static final String TYPE_SYSTEM = "SYSTEM";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_HIGH = "HIGH";

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeLifecycleNotificationService(NotificationRepository notificationRepository,
                                                EmployeeRepository employeeRepository,
                                                UserRepository userRepository,
                                                DepartmentRepository departmentRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public void notifyEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        // 1. Notify employee (if User account exists)
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:JOINED:%d:%d", employee.getId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Welcome to the Organization",
                    "Welcome! Your employee profile has been successfully created.",
                    TYPE_SYSTEM, PRIORITY_MEDIUM, idempotencyKey);
        }

        // 2. Notify manager if assigned
        if (employee.getManager() != null) {
            notifyManagerOfTeamEvent(employee.getManager().getId(), event.organizationId(),
                    "New Team Member Joined",
                    String.format("Employee %s has joined your team.", employee.getFullName()),
                    String.format("LIFECYCLE:JOINED:MGR:%d:%d", employee.getId(), employee.getManager().getId()));
        }
    }

    public void notifyEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        // Notify employee
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:ACTIVATED:%d:%d", employee.getId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Account Activated",
                    "Your employee profile has been activated.",
                    TYPE_SYSTEM, PRIORITY_MEDIUM, idempotencyKey);
        }
    }

    public void notifyEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        String targetDeptName = resolveDepartmentName(event.toDepartmentId(), event.organizationId());
        String deptDisplay = targetDeptName != null ? targetDeptName : "your new department";

        // 1. Notify employee
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:TRANSFERRED:%d:%s:%d",
                    employee.getId(), event.toDepartmentId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Department Transfer Completed",
                    String.format("You have been transferred to %s.", deptDisplay),
                    TYPE_SYSTEM, PRIORITY_MEDIUM, idempotencyKey);
        }

        // 2. Notify manager if assigned
        if (employee.getManager() != null) {
            notifyManagerOfTeamEvent(employee.getManager().getId(), event.organizationId(),
                    "Department Transfer",
                    String.format("Employee %s has transferred to %s.", employee.getFullName(), deptDisplay),
                    String.format("LIFECYCLE:TRANSFERRED:MGR:%d:%s:%d",
                            employee.getId(), event.toDepartmentId(), employee.getManager().getId()));
        }
    }

    public void notifyEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        // 1. Notify employee
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:MGR_CHANGE:%d:%s->%s:%d",
                    employee.getId(), event.oldManagerId(), event.newManagerId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Manager Assignment Updated",
                    "Your reporting manager assignment has been updated.",
                    TYPE_SYSTEM, PRIORITY_MEDIUM, idempotencyKey);
        }

        // 2. Notify new manager if assigned
        if (event.newManagerId() != null) {
            notifyManagerOfTeamEvent(event.newManagerId(), event.organizationId(),
                    "New Direct Report Assigned",
                    String.format("Employee %s has been assigned to report to you.", employee.getFullName()),
                    String.format("LIFECYCLE:NEW_MGR:%d:%d", employee.getId(), event.newManagerId()));
        }

        // 3. Notify old manager if existed
        if (event.oldManagerId() != null) {
            notifyManagerOfTeamEvent(event.oldManagerId(), event.organizationId(),
                    "Direct Report Reassigned",
                    String.format("Employee %s is no longer reporting to you.", employee.getFullName()),
                    String.format("LIFECYCLE:OLD_MGR:%d:%d", employee.getId(), event.oldManagerId()));
        }
    }

    public void notifyEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        String reasonSuffix = (event.reason() != null && !event.reason().isBlank())
                ? " Reason: " + event.reason() : "";

        // 1. Notify employee
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:SUSPENDED:%d:%d", employee.getId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Account Suspended",
                    "Your employee profile has been suspended." + reasonSuffix,
                    TYPE_SYSTEM, PRIORITY_HIGH, idempotencyKey);
        }

        // 2. Notify manager if assigned
        if (employee.getManager() != null) {
            notifyManagerOfTeamEvent(employee.getManager().getId(), event.organizationId(),
                    "Team Member Suspended",
                    String.format("Employee %s has been suspended.", employee.getFullName()),
                    String.format("LIFECYCLE:SUSPENDED:MGR:%d:%d", employee.getId(), employee.getManager().getId()));
        }
    }

    public void notifyEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null) {
            log.warn("[LifecycleNotification] Employee not found for employeeId: {}", event.employeeId());
            return;
        }

        String reasonSuffix = (event.reason() != null && !event.reason().isBlank())
                ? " Reason: " + event.reason() : "";

        // 1. Notify employee
        User employeeUser = findUser(employee.getEmail(), event.organizationId()).orElse(null);
        if (employeeUser != null) {
            String idempotencyKey = String.format("LIFECYCLE:TERMINATED:%d:%d", employee.getId(), employeeUser.getId());
            sendNotification(employeeUser.getId(),
                    "Employment Terminated",
                    "Your employment status has been changed to terminated." + reasonSuffix,
                    TYPE_SYSTEM, PRIORITY_HIGH, idempotencyKey);
        }

        // 2. Notify manager if assigned
        if (employee.getManager() != null) {
            notifyManagerOfTeamEvent(employee.getManager().getId(), event.organizationId(),
                    "Team Member Terminated",
                    String.format("Employee %s has been terminated.", employee.getFullName()),
                    String.format("LIFECYCLE:TERMINATED:MGR:%d:%d", employee.getId(), employee.getManager().getId()));
        }
    }

    private void notifyManagerOfTeamEvent(Long managerEmpId, Long organizationId, String title, String message, String idempotencyKey) {
        Employee managerEmp = findEmployee(managerEmpId, organizationId);
        if (managerEmp == null || managerEmp.getEmail() == null) {
            log.debug("[LifecycleNotification] Manager employee #{} not found or has no email", managerEmpId);
            return;
        }

        User managerUser = findUser(managerEmp.getEmail(), organizationId).orElse(null);
        if (managerUser == null) {
            log.debug("[LifecycleNotification] No User account found for manager email: {}", managerEmp.getEmail());
            return;
        }

        sendNotification(managerUser.getId(), title, message, TYPE_SYSTEM, PRIORITY_MEDIUM, idempotencyKey);
    }

    private void sendNotification(Long userId, String title, String message, String type, String priority, String idempotencyKey) {
        if (userId == null) {
            return;
        }
        if (notificationRepository.existsByIdempotencyKey(idempotencyKey)) {
            log.info("[LifecycleNotification] Notification already exists for idempotencyKey {}: safe no-op", idempotencyKey);
            return;
        }

        try {
            int rowsAffected = notificationRepository.insertNotificationIfNotExists(
                    userId,
                    title,
                    message,
                    type,
                    priority,
                    false,
                    LocalDateTime.now(),
                    idempotencyKey
            );
            if (rowsAffected > 0) {
                log.info("[LifecycleNotification] Created notification for user {} with key {}", userId, idempotencyKey);
            } else {
                log.info("[LifecycleNotification] Duplicate notification prevented by ON CONFLICT for key {}", idempotencyKey);
            }
        } catch (Exception e) {
            log.warn("[LifecycleNotification] Exception inserting notification for key {}: {}", idempotencyKey, e.getMessage());
        }
    }

    private Employee findEmployee(Long employeeId, Long organizationId) {
        if (employeeId == null) return null;
        if (organizationId != null) {
            return employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                    .orElseGet(() -> employeeRepository.findById(employeeId).orElse(null));
        }
        return employeeRepository.findById(employeeId).orElse(null);
    }

    private Optional<User> findUser(String email, Long organizationId) {
        if (email == null || email.isBlank()) return Optional.empty();
        if (organizationId != null) {
            Optional<User> userOpt = userRepository.findByWorkEmailAndOrganizationId(email, organizationId);
            if (userOpt.isPresent()) {
                return userOpt;
            }
        }
        return userRepository.findByWorkEmail(email);
    }

    private String resolveDepartmentName(Long departmentId, Long organizationId) {
        if (departmentId == null) return null;
        if (organizationId != null) {
            return departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                    .map(Department::getName)
                    .orElseGet(() -> departmentRepository.findById(departmentId).map(Department::getName).orElse(null));
        }
        return departmentRepository.findById(departmentId).map(Department::getName).orElse(null);
    }
}
