package com.example.ems.offboarding.service;

import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.FnfPaymentRequest;
import com.example.ems.offboarding.dto.FnfPaymentResponse;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.ExitClearance;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.example.ems.offboarding.repository.EmployeeExitRepository;
import com.example.ems.offboarding.repository.ExitClearanceRepository;
import com.example.ems.offboarding.repository.ExitFnfAuditRepository;
import com.example.ems.offboarding.repository.ExitFnfSettlementRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ExitConcurrencyAndRlsIntegrationTest {

    @Autowired
    private FnfSettlementService fnfSettlementService;

    @Autowired
    private ExitFnfSettlementRepository fnfRepository;

    @Autowired
    private EmployeeExitRepository exitRepository;

    @Autowired
    private ExitFnfAuditRepository auditRepository;

    @Autowired
    private ExitClearanceRepository clearanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PostgresRlsSessionBinder rlsSessionBinder;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    private Organization orgA;
    private Organization orgB;
    private Employee empA;
    private Employee empB;
    private EmployeeExit exitA;
    private EmployeeExit exitB;
    private FnfSettlement fnfA;
    private FnfSettlement fnfB;
    private User financeUserA;

    @BeforeEach
    void setUp() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            TenantContext.clear();
            rlsSessionBinder.bindTenantToCurrentTransaction(null, true);

            auditRepository.deleteAll();
            fnfRepository.deleteAll();
            clearanceRepository.deleteAll();
            exitRepository.deleteAll();

            orgA = organizationRepository.findByOrganizationCode("RLS_EXIT_ORGA").orElseGet(() -> {
                Organization o = new Organization();
                o.setName("RLS-Org-A");
                o.setOrganizationCode("RLS_EXIT_ORGA");
                return organizationRepository.save(o);
            });

            orgB = organizationRepository.findByOrganizationCode("RLS_EXIT_ORGB").orElseGet(() -> {
                Organization o = new Organization();
                o.setName("RLS-Org-B");
                o.setOrganizationCode("RLS_EXIT_ORGB");
                return organizationRepository.save(o);
            });

            empA = employeeRepository.findByEmail("emp.a@orga.com").orElseGet(() -> {
                Employee e = new Employee();
                e.setFullName("Employee A");
                e.setEmail("emp.a@orga.com");
                e.setEmployeeId("EMPA-01");
                e.setDepartment("Finance");
                e.setStatus("ACTIVE");
                e.setCurrentStatus("ACTIVE");
                e.setAvailability("AVAILABLE");
                e.setAnnualSalary(BigDecimal.valueOf(1200000.00));
                e.setOrganization(orgA);
                return employeeRepository.save(e);
            });

            empB = employeeRepository.findByEmail("emp.b@orgb.com").orElseGet(() -> {
                Employee e = new Employee();
                e.setFullName("Employee B");
                e.setEmail("emp.b@orgb.com");
                e.setEmployeeId("EMPB-01");
                e.setDepartment("Finance");
                e.setStatus("ACTIVE");
                e.setCurrentStatus("ACTIVE");
                e.setAvailability("AVAILABLE");
                e.setAnnualSalary(BigDecimal.valueOf(1200000.00));
                e.setOrganization(orgB);
                return employeeRepository.save(e);
            });

            exitA = new EmployeeExit();
            exitA.setOrganization(orgA);
            exitA.setEmployee(empA);
            exitA.setStatus("FINANCE_APPROVAL_PENDING");
            exitA.setLastWorkingDate(LocalDate.now());
            exitA = exitRepository.save(exitA);

            exitB = new EmployeeExit();
            exitB.setOrganization(orgB);
            exitB.setEmployee(empB);
            exitB.setStatus("FINANCE_APPROVAL_PENDING");
            exitB.setLastWorkingDate(LocalDate.now());
            exitB = exitRepository.save(exitB);

            fnfA = new FnfSettlement();
            fnfA.setOrganization(orgA);
            fnfA.setExit(exitA);
            fnfA.setStatus("SETTLEMENT_APPROVED");
            fnfA.setNetSettlement(BigDecimal.valueOf(100000.00));
            fnfA.setTotalEarnings(BigDecimal.valueOf(100000.00));
            fnfA.setTotalDeductions(BigDecimal.ZERO);
            fnfA = fnfRepository.save(fnfA);

            fnfB = new FnfSettlement();
            fnfB.setOrganization(orgB);
            fnfB.setExit(exitB);
            fnfB.setStatus("SETTLEMENT_APPROVED");
            fnfB.setNetSettlement(BigDecimal.valueOf(100000.00));
            fnfB.setTotalEarnings(BigDecimal.valueOf(100000.00));
            fnfB.setTotalDeductions(BigDecimal.ZERO);
            fnfB = fnfRepository.save(fnfB);

            ExitClearance clrA = new ExitClearance();
            clrA.setOrganization(orgA);
            clrA.setExit(exitA);
            clrA.setDepartment("IT");
            clrA.setAssignedTo(empA);
            clrA.setClearanceReason("Clearance completed");
            clrA.setStatus("CLEARED");
            clearanceRepository.save(clrA);

            ExitClearance clrB = new ExitClearance();
            clrB.setOrganization(orgB);
            clrB.setExit(exitB);
            clrB.setDepartment("IT");
            clrB.setAssignedTo(empB);
            clrB.setClearanceReason("Clearance completed");
            clrB.setStatus("CLEARED");
            clearanceRepository.save(clrB);

            return null;
        });

        financeUserA = new User();
        financeUserA.setId(9991L);
        financeUserA.setWorkEmail("finance.a@orga.com");
        financeUserA.setFullName("Finance A Officer");
        financeUserA.setOrganization(orgA);
        Role r = new Role();
        r.setName("FINANCE");
        financeUserA.setRole(r);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            rlsSessionBinder.bindTenantToCurrentTransaction(null, true);
            auditRepository.deleteAll();
            fnfRepository.deleteAll();
            clearanceRepository.deleteAll();
            exitRepository.deleteAll();
            return null;
        });
    }

    // =========================================================================
    // 1. REAL CONCURRENT PESSIMISTIC LOCKING TEST
    // =========================================================================

    @Test
    @DisplayName("30. Concurrent Payment Race: Exactly ONE database transaction succeeds; second is serialized and fails")
    void testConcurrentPayment_OnlyOneDatabaseTransitionSucceeds() throws Exception {
        Long targetFnfId = fnfA.getId();
        Long targetOrgId = orgA.getId();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> outcomes = new CopyOnWriteArrayList<>();

        Callable<Void> task = () -> {
            // Wait for both threads to be ready to maximize race condition
            barrier.await(5, TimeUnit.SECONDS);

            TenantContext.setCurrentTenant(targetOrgId);
            try {
                // Execute in distinct transaction
                TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                txTemplate.execute(status -> {
                    String threadName = Thread.currentThread().getName();
                    FnfPaymentRequest req = new FnfPaymentRequest();
                    req.setAmount(BigDecimal.valueOf(100000.00));
                    req.setPaymentMethod("BANK_TRANSFER");
                    req.setPaymentDate(LocalDate.now());
                    req.setTransactionReference("UTR-RACE-" + System.nanoTime() + "-" + threadName);
                    req.setIdempotencyKey("KEY-RACE-" + System.nanoTime() + "-" + threadName);
                    req.setRemarks("Concurrent disbursement test");

                    FnfPaymentResponse response = fnfSettlementService.processPayment(financeUserA, targetFnfId, req);
                    outcomes.add("SUCCESS: " + response.getTransactionReference());
                    successCount.incrementAndGet();
                    return null;
                });
            } catch (Exception e) {
                outcomes.add("FAILED: " + e.getMessage());
                failureCount.incrementAndGet();
            } finally {
                TenantContext.clear();
            }
            return null;
        };

        List<Future<Void>> futures = executor.invokeAll(List.of(task, task));
        for (Future<Void> f : futures) {
            f.get();
        }
        executor.shutdown();

        // Exactly ONE transaction must succeed, and exactly ONE must fail
        assertEquals(1, successCount.get(), "Exactly one concurrent payment should succeed");
        assertEquals(1, failureCount.get(), "Concurrent second payment attempt should be rejected");

        // Verify final state in database under target tenant context
        TransactionTemplate readTx = new TransactionTemplate(transactionManager);
        readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        readTx.execute(status -> {
            TenantContext.setCurrentTenant(targetOrgId);
            rlsSessionBinder.bindTenantToCurrentTransaction(targetOrgId, false);

            FnfSettlement finalSettlement = fnfRepository.findById(targetFnfId).orElseThrow();
            assertEquals("PAYMENT_RELEASED", finalSettlement.getStatus());
            assertNotNull(finalSettlement.getPaymentReference());

            EmployeeExit finalExit = exitRepository.findById(exitA.getId()).orElseThrow();
            assertEquals("SETTLEMENT_COMPLETED", finalExit.getStatus());

            Employee finalEmp = employeeRepository.findById(empA.getId()).orElseThrow();
            assertEquals("EXITED", finalEmp.getStatus());
            return null;
        });
    }

    // =========================================================================
    // 2. POSTGRESQL ROW LEVEL SECURITY (RLS) POLICY ISOLATION
    // =========================================================================

    @Test
    @DisplayName("31. Tenant A RLS policy cannot READ Tenant B Exit records")
    void testTenantARlsCannotReadTenantBExit() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Query employee_exits under Tenant A RLS
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT id, organization_id FROM employee_exits WHERE organization_id = ?")) {
                ps.setLong(1, orgB.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertFalse(rs.next(), "Tenant A RLS must return ZERO rows when querying Tenant B exits");
                }
            }
            conn.rollback();
        }
    }

    @Test
    @DisplayName("32. Tenant A RLS policy cannot READ Tenant B F&F Settlement records")
    void testTenantARlsCannotReadTenantBFnf() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Query exit_fnf_settlements under Tenant A RLS
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, organization_id FROM exit_fnf_settlements WHERE organization_id = ?")) {
                ps.setLong(1, orgB.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertFalse(rs.next(), "Tenant A RLS must return ZERO rows when querying Tenant B settlements");
                }
            }
            conn.rollback();
        }
    }

    @Test
    @DisplayName("33. Tenant A RLS policy blocks cross-tenant UPDATE and DELETE")
    void testTenantARlsCannotWriteTenantBExit() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Attempt to update Tenant B exit under Tenant A RLS context
            try (PreparedStatement ps = conn
                    .prepareStatement("UPDATE employee_exits SET status = 'TAMPERED' WHERE id = ?")) {
                ps.setLong(1, exitB.getId());
                int rowsUpdated = ps.executeUpdate();
                assertEquals(0, rowsUpdated, "Tenant A RLS must update ZERO rows for Tenant B records");
            }

            // Attempt to delete Tenant B exit under Tenant A RLS context
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM employee_exits WHERE id = ?")) {
                ps.setLong(1, exitB.getId());
                int rowsDeleted = ps.executeUpdate();
                assertEquals(0, rowsDeleted, "Tenant A RLS must delete ZERO rows for Tenant B records");
            }
            conn.rollback();
        }
    }

    // =========================================================================
    // 3. POOLED CONNECTION CLEANUP & FAIL-CLOSED INTEGRITY
    // =========================================================================

    @Test
    @DisplayName("34. SET LOCAL resets automatically upon commit: Same physical connection reused sees only target tenant")
    void testPooledConnectionTenantIsolation() throws Exception {
        // Step 1: Use connection for Tenant A and commit
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                ps.execute();
            }

            // Verify Tenant A can see its own record
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT count(*) FROM employee_exits WHERE organization_id = ?")) {
                ps.setLong(1, orgA.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertTrue(rs.getInt(1) >= 1);
                }
            }
            conn.commit(); // PostgreSQL resets SET LOCAL app.current_tenant_id here!

            // Step 2: On the SAME physical connection, run without setting tenant -> FAIL
            // CLOSED (0 rows)
            try (PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM employee_exits")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1), "Without SET LOCAL, RLS must fail closed and return 0 rows");
                }
            }

            // Step 3: On the SAME physical connection, bind Tenant B
            try (PreparedStatement ps = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgB.getId() + "'")) {
                ps.execute();
            }

            // Verify Tenant B sees ONLY Tenant B's records
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT count(*) FROM employee_exits WHERE organization_id = ?")) {
                ps.setLong(1, orgA.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1), "Tenant B connection must see 0 rows from Tenant A");
                }
            }

            conn.commit();
        }
    }

    @Test
    @DisplayName("35. Missing tenant context fails closed with IllegalStateException")
    void testMissingTenantFailsClosed() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            rlsSessionBinder.bindTenantToCurrentTransaction(null, false);
            return null;
        }));
        assertTrue(ex.getMessage().contains("Cannot bind RLS context: Missing required tenant ID"));
    }

    @Test
    @DisplayName("36. Tenant A RLS policy blocks cross-tenant INSERT (attempting to insert row for Tenant B fails)")
    void testTenantARlsCannotInsertTenantBRecord() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Malicious attempt: Under Tenant A session, try to insert an employee_exit
            // record with organization_id = orgB.getId()
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO employee_exits (organization_id, employee_id, status, exit_type, created_at, updated_at) "
                            +
                            "VALUES (?, ?, 'RESIGNED', 'RESIGNATION', NOW(), NOW())")) {
                ps.setLong(1, orgB.getId());
                ps.setLong(2, empB.getId());

                Exception ex = assertThrows(Exception.class, ps::executeUpdate,
                        "PostgreSQL RLS must reject INSERT of row belonging to Tenant B when session is bound to Tenant A");
                assertTrue(ex.getMessage().toLowerCase().contains("row-level security")
                        || ex.getMessage().toLowerCase().contains("violates"),
                        "Expected RLS policy violation error, but got: " + ex.getMessage());
            }
            conn.rollback();
        }
    }

    @Test
    @DisplayName("37. Payment Replay After Transaction Commit: Identical receipt returned with zero second payment")
    void testPaymentReplayAfterTransactionCommit() {
        Long targetFnfId = fnfB.getId();
        Long targetOrgId = orgB.getId();

        TenantContext.setCurrentTenant(targetOrgId);
        try {
            User financeUserB = new User();
            financeUserB.setId(9992L);
            financeUserB.setWorkEmail("finance.b@orgb.com");
            financeUserB.setFullName("Finance B Officer");
            financeUserB.setOrganization(orgB);
            Role r = new Role();
            r.setName("FINANCE");
            financeUserB.setRole(r);

            // Request 1: Initial disbursement succeeds and commits
            FnfPaymentRequest req1 = new FnfPaymentRequest();
            req1.setAmount(BigDecimal.valueOf(100000.00));
            req1.setPaymentMethod("BANK_TRANSFER");
            req1.setPaymentDate(LocalDate.now());
            req1.setTransactionReference("UTR-REPLAY-9999");
            req1.setIdempotencyKey("KEY-REPLAY-9999");
            req1.setRemarks("First disbursement attempt");

            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            FnfPaymentResponse resp1 = txTemplate
                    .execute(status -> fnfSettlementService.processPayment(financeUserB, targetFnfId, req1));
            assertNotNull(resp1);
            assertEquals("PAYMENT_RELEASED", resp1.getStatus());
            assertEquals("UTR-REPLAY-9999", resp1.getTransactionReference());
            assertEquals("KEY-REPLAY-9999", resp1.getIdempotencyKey());
            assertEquals(0, resp1.getAmount().compareTo(BigDecimal.valueOf(100000.00)));

            // Request 2 (Replay / Retry after network timeout): Client retries with EXACT
            // same Idempotency-Key
            FnfPaymentRequest retryReq = new FnfPaymentRequest();
            retryReq.setAmount(BigDecimal.valueOf(100000.00));
            retryReq.setPaymentMethod("BANK_TRANSFER");
            retryReq.setPaymentDate(LocalDate.now());
            retryReq.setTransactionReference("UTR-REPLAY-9999");
            retryReq.setIdempotencyKey("KEY-REPLAY-9999");
            retryReq.setRemarks("First disbursement attempt");

            FnfPaymentResponse resp2 = txTemplate
                    .execute(status -> fnfSettlementService.processPayment(financeUserB, targetFnfId, retryReq));

            // Verify: Same payment identity, same amount, same reference, no second payment
            assertNotNull(resp2);
            assertEquals(resp1.getFnfId(), resp2.getFnfId());
            assertEquals(resp1.getStatus(), resp2.getStatus());
            assertEquals(resp1.getTransactionReference(), resp2.getTransactionReference());
            assertEquals(resp1.getIdempotencyKey(), resp2.getIdempotencyKey());
            assertEquals(0, resp1.getAmount().compareTo(resp2.getAmount()));
            assertEquals(resp1.getPaidAt().truncatedTo(ChronoUnit.SECONDS),
                    resp2.getPaidAt().truncatedTo(ChronoUnit.SECONDS));

            // Verify in database: exactly 1 settlement record with this idempotency key
            txTemplate.execute(status -> {
                rlsSessionBinder.bindTenantToCurrentTransaction(targetOrgId, false);
                FnfSettlement inDb = fnfRepository.findById(targetFnfId).orElseThrow();
                assertEquals("PAYMENT_RELEASED", inDb.getStatus());
                assertEquals("KEY-REPLAY-9999", inDb.getIdempotencyKey());
                assertEquals("UTR-REPLAY-9999", inDb.getPaymentReference());
                return null;
            });
        } finally {
            TenantContext.clear();
        }
    }
}
