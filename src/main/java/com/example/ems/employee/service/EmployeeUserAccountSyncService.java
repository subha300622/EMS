package com.example.ems.employee.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.SessionService;
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

import java.util.Optional;

/**
 * Service responsible for synchronizing Employee lifecycle changes into the
 * associated User authentication account within an independent transaction (REQUIRES_NEW).
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class EmployeeUserAccountSyncService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeUserAccountSyncService.class);

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SessionService sessionService;

    public EmployeeUserAccountSyncService(EmployeeRepository employeeRepository,
                                          UserRepository userRepository,
                                          DepartmentRepository departmentRepository,
                                          SessionService sessionService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.sessionService = sessionService;
    }

    public void syncOnEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.debug("[UserSync] EmployeeJoinedEvent is a no-op for User account sync (employee: {})", event.employeeId());
    }

    public void syncOnEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSync] Synchronizing EmployeeActivatedEvent for employee: {}", event.employeeId());
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null || employee.getEmail() == null) {
            log.warn("[UserSync] Employee not found or missing email for employeeId: {}", event.employeeId());
            return;
        }

        Optional<User> userOpt = findUser(employee.getEmail(), event.organizationId());
        if (userOpt.isEmpty()) {
            log.info("[UserSync] No User account exists for work email: {}, skipping activation sync", employee.getEmail());
            return;
        }

        User user = userOpt.get();
        user.setStatus("ACTIVE");
        userRepository.save(user);
        log.info("[UserSync] Successfully activated User account id: {} for employee: {}", user.getId(), event.employeeId());
    }

    public void syncOnEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSync] Synchronizing EmployeeSuspendedEvent for employee: {}", event.employeeId());
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null || employee.getEmail() == null) {
            log.warn("[UserSync] Employee not found or missing email for employeeId: {}", event.employeeId());
            return;
        }

        Optional<User> userOpt = findUser(employee.getEmail(), event.organizationId());
        if (userOpt.isEmpty()) {
            log.info("[UserSync] No User account exists for work email: {}, skipping suspension sync", employee.getEmail());
            return;
        }

        User user = userOpt.get();
        user.setStatus("SUSPENDED");
        userRepository.save(user);

        if (user.getUserId() != null && !user.getUserId().isBlank()) {
            sessionService.revokeAllSessions(user.getUserId());
            log.info("[UserSync] Revoked all active sessions for suspended user: {}", user.getUserId());
        }
    }

    public void syncOnEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSync] Synchronizing EmployeeTerminatedEvent for employee: {}", event.employeeId());
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null || employee.getEmail() == null) {
            log.warn("[UserSync] Employee not found or missing email for employeeId: {}", event.employeeId());
            return;
        }

        Optional<User> userOpt = findUser(employee.getEmail(), event.organizationId());
        if (userOpt.isEmpty()) {
            log.info("[UserSync] No User account exists for work email: {}, skipping termination sync", employee.getEmail());
            return;
        }

        User user = userOpt.get();
        user.setStatus("INACTIVE");
        userRepository.save(user);

        if (user.getUserId() != null && !user.getUserId().isBlank()) {
            sessionService.revokeAllSessions(user.getUserId());
            log.info("[UserSync] Revoked all active sessions for terminated user: {}", user.getUserId());
        }
    }

    public void syncOnEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSync] Synchronizing EmployeeTransferredEvent for employee: {}, toDept: {}",
                event.employeeId(), event.toDepartmentId());
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null || employee.getEmail() == null) {
            log.warn("[UserSync] Employee not found or missing email for employeeId: {}", event.employeeId());
            return;
        }

        Optional<User> userOpt = findUser(employee.getEmail(), event.organizationId());
        if (userOpt.isEmpty()) {
            log.info("[UserSync] No User account exists for work email: {}, skipping department transfer sync", employee.getEmail());
            return;
        }

        User user = userOpt.get();
        Long targetDeptId = event.toDepartmentId();
        user.setDepartmentId(targetDeptId);

        if (targetDeptId != null) {
            String deptName = resolveDepartmentName(targetDeptId, event.organizationId());
            user.setDepartment(deptName);
        } else {
            user.setDepartment(null);
        }

        userRepository.save(user);
        log.info("[UserSync] Updated department for User id: {} to deptId: {}, deptName: {}",
                user.getId(), targetDeptId, user.getDepartment());
    }

    public void syncOnEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[UserSync] Synchronizing EmployeeManagerChangedEvent for employee: {}, newManager: {}",
                event.employeeId(), event.newManagerId());
        Employee employee = findEmployee(event.employeeId(), event.organizationId());
        if (employee == null || employee.getEmail() == null) {
            log.warn("[UserSync] Employee not found or missing email for employeeId: {}", event.employeeId());
            return;
        }

        Optional<User> userOpt = findUser(employee.getEmail(), event.organizationId());
        if (userOpt.isEmpty()) {
            log.info("[UserSync] No User account exists for work email: {}, skipping manager change sync", employee.getEmail());
            return;
        }

        User user = userOpt.get();
        Long newManagerEmpId = event.newManagerId();

        if (newManagerEmpId != null) {
            Employee managerEmp = findEmployee(newManagerEmpId, event.organizationId());
            if (managerEmp != null && managerEmp.getEmail() != null) {
                Optional<User> managerUserOpt = findUser(managerEmp.getEmail(), event.organizationId());
                user.setReportingManagerId(managerUserOpt.map(User::getId).orElse(null));
            } else {
                user.setReportingManagerId(null);
            }
        } else {
            user.setReportingManagerId(null);
        }

        userRepository.save(user);
        log.info("[UserSync] Updated reportingManagerId for User id: {} to: {}", user.getId(), user.getReportingManagerId());
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
