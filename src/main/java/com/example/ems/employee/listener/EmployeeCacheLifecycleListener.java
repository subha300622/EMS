package com.example.ems.employee.listener;

import com.example.ems.employee.entity.Department;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.service.EmployeeCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transaction-aware listener that invalidates targeted employee, department, manager,
 * and organizational dashboard caches AFTER_COMMIT upon employee lifecycle events.
 */
@Component
public class EmployeeCacheLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(EmployeeCacheLifecycleListener.class);

    private final EmployeeCacheService employeeCacheService;
    private final DepartmentRepository departmentRepository;

    public EmployeeCacheLifecycleListener(EmployeeCacheService employeeCacheService,
                                         DepartmentRepository departmentRepository) {
        this.employeeCacheService = employeeCacheService;
        this.departmentRepository = departmentRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeJoined(EmployeeJoinedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeJoinedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        employeeCacheService.evictEmployeeLifecycle(event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeActivated(EmployeeActivatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeActivatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        employeeCacheService.evictEmployeeLifecycle(event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeSuspended(EmployeeSuspendedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeSuspendedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        employeeCacheService.evictEmployeeLifecycle(event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeTerminated(EmployeeTerminatedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeTerminatedEvent AFTER_COMMIT for employee: {}", event.employeeId());
        employeeCacheService.evictEmployeeLifecycle(event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeManagerChanged(EmployeeManagerChangedEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeManagerChangedEvent AFTER_COMMIT for employee: {}, oldManager: {}, newManager: {}",
                event.employeeId(), event.oldManagerId(), event.newManagerId());
        employeeCacheService.evictManagerChange(event.employeeId(), event.oldManagerId(), event.newManagerId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeTransferred(EmployeeTransferredEvent event) {
        if (event == null || event.employeeId() == null) {
            return;
        }
        log.info("[LifecycleCache] Handling EmployeeTransferredEvent AFTER_COMMIT for employee: {}, fromDept: {}, toDept: {}",
                event.employeeId(), event.fromDepartmentId(), event.toDepartmentId());
        String oldDeptName = resolveDepartmentName(event.fromDepartmentId(), event.organizationId());
        String newDeptName = resolveDepartmentName(event.toDepartmentId(), event.organizationId());
        employeeCacheService.evictDepartmentTransfer(event.employeeId(), oldDeptName, newDeptName);
    }

    private String resolveDepartmentName(Long departmentId, Long organizationId) {
        if (departmentId == null) {
            return null;
        }
        try {
            if (organizationId != null) {
                return departmentRepository.findByIdAndOrganizationId(departmentId, organizationId)
                        .map(Department::getName)
                        .orElseGet(() -> departmentRepository.findById(departmentId).map(Department::getName).orElse(null));
            }
            return departmentRepository.findById(departmentId).map(Department::getName).orElse(null);
        } catch (Exception e) {
            log.warn("[LifecycleCache] Failed to resolve department name for id: {}", departmentId, e);
            return null;
        }
    }
}
