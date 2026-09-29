package com.example.ems.employee.listener;

import com.example.ems.employee.entity.Department;
import com.example.ems.employee.event.*;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.service.EmployeeCacheService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class EmployeeCacheLifecycleListenerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public EmployeeCacheService testEmployeeCacheService() {
            return mock(EmployeeCacheService.class);
        }
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EmployeeCacheService employeeCacheService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeCacheLifecycleListener listener;

    private TransactionTemplate transactionTemplate;
    private Organization testOrg;
    private Department deptEngineering;
    private Department deptProduct;
    private final Instant now = Instant.now();

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        reset(employeeCacheService);

        testOrg = organizationRepository.findByOrganizationCode("CACHE_TEST_ORG").orElseGet(() -> {
            Organization org = new Organization();
            org.setName("Cache Test Org");
            org.setOrganizationCode("CACHE_TEST_ORG");
            return organizationRepository.save(org);
        });

        deptEngineering = departmentRepository.findByNameIgnoreCaseAndOrganizationId("CacheTestEngineering", testOrg.getId())
                .orElseGet(() -> {
                    Department d = new Department();
                    d.setName("CacheTestEngineering");
                    d.setCode("ENG_CACHE");
                    d.setOrganization(testOrg);
                    return departmentRepository.save(d);
                });

        deptProduct = departmentRepository.findByNameIgnoreCaseAndOrganizationId("CacheTestProduct", testOrg.getId())
                .orElseGet(() -> {
                    Department d = new Department();
                    d.setName("CacheTestProduct");
                    d.setCode("PRD_CACHE");
                    d.setOrganization(testOrg);
                    return departmentRepository.save(d);
                });
    }

    // =========================================================================
    // 6 EVENT-SPECIFIC TESTS
    // =========================================================================

    @Test
    @DisplayName("1. EmployeeJoinedEvent: triggers lifecycle cache eviction after commit")
    void employeeJoinedEvent_triggersLifecycleEviction() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeJoinedEvent(101L, testOrg.getId(), now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictEmployeeLifecycle(101L);
    }

    @Test
    @DisplayName("2. EmployeeActivatedEvent: triggers lifecycle cache eviction after commit")
    void employeeActivatedEvent_triggersLifecycleEviction() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeActivatedEvent(102L, testOrg.getId(), now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictEmployeeLifecycle(102L);
    }

    @Test
    @DisplayName("3. EmployeeSuspendedEvent: triggers lifecycle cache eviction after commit")
    void employeeSuspendedEvent_triggersLifecycleEviction() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeSuspendedEvent(103L, testOrg.getId(), "Disciplinary inquiry", now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictEmployeeLifecycle(103L);
    }

    @Test
    @DisplayName("4. EmployeeTerminatedEvent: triggers lifecycle cache eviction after commit")
    void employeeTerminatedEvent_triggersLifecycleEviction() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeTerminatedEvent(104L, testOrg.getId(), "F&F payment disbursed", now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictEmployeeLifecycle(104L);
    }

    @Test
    @DisplayName("5. EmployeeManagerChangedEvent: invalidates employee, old manager, and new manager caches")
    void employeeManagerChangedEvent_invalidatesOldAndNewManagerCaches() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeManagerChangedEvent(105L, testOrg.getId(), 201L, 202L, now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictManagerChange(105L, 201L, 202L);
    }

    @Test
    @DisplayName("6. EmployeeTransferredEvent: resolves department names and invalidates organizational caches")
    void employeeTransferredEvent_invalidatesAffectedDepartmentCaches() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeTransferredEvent(
                    106L,
                    testOrg.getId(),
                    deptEngineering.getId(),
                    deptProduct.getId(),
                    now
            ));
            return null;
        });

        verify(employeeCacheService, times(1)).evictDepartmentTransfer(106L, "CacheTestEngineering", "CacheTestProduct");
    }

    // =========================================================================
    // TRANSACTIONAL PHASE & ROBUSTNESS TESTS
    // =========================================================================

    @Test
    @DisplayName("7. Verifies listener executes only AFTER_COMMIT")
    void listenerExecutesOnlyAfterCommit() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeActivatedEvent(107L, testOrg.getId(), now));
            // Inside transaction prior to commit: listener must NOT have fired yet
            verifyNoInteractions(employeeCacheService);
            return null;
        });

        // After commit: eviction must have executed
        verify(employeeCacheService, times(1)).evictEmployeeLifecycle(107L);
    }

    @Test
    @DisplayName("8. Verifies rollback does NOT trigger cache eviction")
    void rollbackDoesNotEvict() {
        try {
            transactionTemplate.execute(status -> {
                eventPublisher.publishEvent(new EmployeeTerminatedEvent(108L, testOrg.getId(), "Aborted exit", now));
                // Force transaction rollback
                status.setRollbackOnly();
                return null;
            });
        } catch (Exception ignored) {
        }

        // After rollback: AFTER_COMMIT listener must NOT have fired
        verifyNoInteractions(employeeCacheService);
    }

    @Test
    @DisplayName("9. Manager change with null old or new manager ID executes safely")
    void managerChangeWithNullManagers_doesNotFail() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeManagerChangedEvent(109L, testOrg.getId(), null, 203L, now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictManagerChange(109L, null, 203L);
    }

    @Test
    @DisplayName("10. Department transfer with null department IDs or non-existent IDs executes safely")
    void transferWithNullDepartments_doesNotFail() {
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(new EmployeeTransferredEvent(110L, testOrg.getId(), null, 999999L, now));
            return null;
        });

        verify(employeeCacheService, times(1)).evictDepartmentTransfer(110L, null, null);
    }

    @Test
    @DisplayName("11. Direct invocation with null event or null employeeId is a safe no-op")
    void nullEventOrNullEmployeeId_safeNoOp() {
        assertDoesNotThrow(() -> listener.handleEmployeeJoined(null));
        assertDoesNotThrow(() -> listener.handleEmployeeActivated(new EmployeeActivatedEvent(null, testOrg.getId(), now)));
        assertDoesNotThrow(() -> listener.handleEmployeeSuspended(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTerminated(new EmployeeTerminatedEvent(null, testOrg.getId(), "r", now)));
        assertDoesNotThrow(() -> listener.handleEmployeeManagerChanged(null));
        assertDoesNotThrow(() -> listener.handleEmployeeTransferred(null));

        verifyNoInteractions(employeeCacheService);
    }
}
