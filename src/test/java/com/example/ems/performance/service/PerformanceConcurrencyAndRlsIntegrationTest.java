package com.example.ems.performance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.performance.entity.*;
import com.example.ems.performance.repository.*;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.rls.PostgresRlsSessionBinder;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceConcurrencyAndRlsIntegrationTest {

    @Autowired
    private PerformanceReviewRecordRepository reviewRepository;
    @Autowired
    private PerformanceReviewCycleRepository cycleRepository;
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
    private jakarta.persistence.EntityManager entityManager;

    private Organization orgA;
    private Organization orgB;
    private Employee empA;
    private Employee empB;
    private PerformanceReviewCycle cycleA;
    private PerformanceReviewCycle cycleB;
    private PerformanceReviewRecord reviewA;

    @BeforeEach
    void setUp() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.execute(status -> {
            TenantContext.clear();
            rlsSessionBinder.bindTenantToCurrentTransaction(null, true);

            orgA = organizationRepository.findByOrganizationCode("RLS_PERF_ORGA").orElseGet(() -> {
                Organization o = new Organization();
                o.setName("RLS-Perf-A");
                o.setOrganizationCode("RLS_PERF_ORGA");
                return organizationRepository.save(o);
            });

            orgB = organizationRepository.findByOrganizationCode("RLS_PERF_ORGB").orElseGet(() -> {
                Organization o = new Organization();
                o.setName("RLS-Perf-B");
                o.setOrganizationCode("RLS_PERF_ORGB");
                return organizationRepository.save(o);
            });

            empA = employeeRepository.findByEmail("perf.a@orga.com").orElseGet(() -> {
                Employee e = new Employee();
                e.setFullName("Perf Employee A");
                e.setEmail("perf.a@orga.com");
                e.setEmployeeId("PERFA-01");
                e.setDepartment("Engineering");
                e.setStatus("ACTIVE");
                e.setOrganization(orgA);
                return employeeRepository.save(e);
            });

            empB = employeeRepository.findByEmail("perf.b@orgb.com").orElseGet(() -> {
                Employee e = new Employee();
                e.setFullName("Perf Employee B");
                e.setEmail("perf.b@orgb.com");
                e.setEmployeeId("PERFB-01");
                e.setDepartment("Engineering");
                e.setStatus("ACTIVE");
                e.setOrganization(orgB);
                return employeeRepository.save(e);
            });

            cycleA = cycleRepository.findByOrganizationIdAndCode(orgA.getId(), "CYC-A-2026").orElseGet(() -> {
                PerformanceReviewCycle c = new PerformanceReviewCycle();
                c.setOrganization(orgA);
                c.setName("Cycle A");
                c.setCode("CYC-A-2026");
                c.setPeriodType("QUARTERLY");
                c.setStartDate(LocalDate.of(2026, 1, 1));
                c.setEndDate(LocalDate.of(2026, 3, 31));
                c.setStatus("ACTIVE");
                return cycleRepository.save(c);
            });

            cycleB = cycleRepository.findByOrganizationIdAndCode(orgB.getId(), "CYC-B-2026").orElseGet(() -> {
                PerformanceReviewCycle c = new PerformanceReviewCycle();
                c.setOrganization(orgB);
                c.setName("Cycle B");
                c.setCode("CYC-B-2026");
                c.setPeriodType("QUARTERLY");
                c.setStartDate(LocalDate.of(2026, 1, 1));
                c.setEndDate(LocalDate.of(2026, 3, 31));
                c.setStatus("ACTIVE");
                return cycleRepository.save(c);
            });

            reviewA = reviewRepository
                    .findByOrganizationIdAndCycleIdAndEmployeeId(orgA.getId(), cycleA.getId(), empA.getId())
                    .orElseGet(() -> {
                        PerformanceReviewRecord r = new PerformanceReviewRecord();
                        r.setOrganization(orgA);
                        r.setCycle(cycleA);
                        r.setEmployee(empA);
                        r.setStatus("DRAFT");
                        return reviewRepository.save(r);
                    });
            reviewA.setStatus("DRAFT");
            reviewA.setSubmittedAt(null);
            reviewA.setApprovedAt(null);
            reviewA.setRejectedAt(null);
            reviewA.setPublishedAt(null);
            reviewA.setLockedAt(null);
            reviewA = reviewRepository.save(reviewA);

            return null;
        });
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("RLS Tenant Isolation: Tenant B session cannot view Tenant A performance reviews")
    void testRlsPolicy_DirectSqlTenantSeparation() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Bound to Tenant B
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgB.getId() + "'")) {
                setLocal.execute();
            }

            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM performance_review_records WHERE id = ?")) {
                ps.setLong(1, reviewA.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1),
                            "RLS must filter out review belonging to Tenant A when session is Tenant B");
                }
            }

            // 2. Bound to Tenant A
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM performance_review_records WHERE id = ?")) {
                ps.setLong(1, reviewA.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(1, rs.getInt(1),
                            "RLS must return review belonging to Tenant A when session is Tenant A");
                }
            }

            conn.rollback();
        }
    }

    @Test
    @DisplayName("RLS Fail-Closed: Unset tenant session returns 0 rows")
    void testRlsPolicy_FailsClosedWhenTenantIdNullOrEmpty() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            // app.current_tenant_id unset
            try (PreparedStatement setLocal = conn.prepareStatement("SET LOCAL app.current_tenant_id = ''")) {
                setLocal.execute();
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM performance_review_records")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1),
                            "RLS must fail-closed and return 0 rows when tenant context is unset");
                }
            }

            conn.rollback();
        }
    }

    @Test
    @DisplayName("Composite Foreign Key: Database rejects review referencing Cycle of another tenant")
    void testCompositeForeignKey_CrossTenantCycleRejected() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Attempt: insert review with organization_id = orgA, but cycle_id = cycleB
            // (which belongs to orgB)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO performance_review_records (organization_id, cycle_id, employee_id, status, created_at, updated_at) "
                            +
                            "VALUES (?, ?, ?, 'DRAFT', NOW(), NOW())")) {
                ps.setLong(1, orgA.getId());
                ps.setLong(2, cycleB.getId()); // Cycle of Org B!
                ps.setLong(3, empA.getId());

                Exception ex = assertThrows(Exception.class, ps::executeUpdate,
                        "Database composite FK must reject review referencing cycle from another organization");
                assertTrue(ex.getMessage().toLowerCase().contains("foreign key") ||
                        ex.getMessage().toLowerCase().contains("violates") ||
                        ex.getMessage().toLowerCase().contains("fk_perf_review_cycle"),
                        "Expected foreign key constraint violation, but got: " + ex.getMessage());
            }
            conn.rollback();
        }
    }

    @Test
    @DisplayName("Composite Foreign Key: Database rejects review referencing Employee of another tenant")
    void testCompositeForeignKey_CrossTenantEmployeeRejected() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            // Attempt: insert review with organization_id = orgA, but employee_id = empB
            // (belongs to orgB)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO performance_review_records (organization_id, cycle_id, employee_id, status, created_at, updated_at) "
                            +
                            "VALUES (?, ?, ?, 'DRAFT', NOW(), NOW())")) {
                ps.setLong(1, orgA.getId());
                ps.setLong(2, cycleA.getId());
                ps.setLong(3, empB.getId()); // Employee of Org B!

                Exception ex = assertThrows(Exception.class, ps::executeUpdate,
                        "Database composite FK must reject review referencing employee from another organization");
                assertTrue(ex.getMessage().toLowerCase().contains("foreign key") ||
                        ex.getMessage().toLowerCase().contains("violates") ||
                        ex.getMessage().toLowerCase().contains("fk_perf_review_emp"),
                        "Expected foreign key constraint violation, but got: " + ex.getMessage());
            }
            conn.rollback();
        }
    }

    @Test
    @DisplayName("Audit Immutability Trigger: Database blocks direct UPDATE and DELETE on performance_review_audits")
    void testAuditTable_UpdateOrDeleteRejectedByDatabaseTrigger() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement setLocal = conn
                    .prepareStatement("SET LOCAL app.current_tenant_id = '" + orgA.getId() + "'")) {
                setLocal.execute();
            }

            long auditId;
            // 1. Insert an audit record
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO performance_review_audits (organization_id, review_id, action, actor_name, created_at) "
                            +
                            "VALUES (?, ?, 'TEST_AUDIT', 'Admin', NOW()) RETURNING id")) {
                ps.setLong(1, orgA.getId());
                ps.setLong(2, reviewA.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    auditId = rs.getLong(1);
                }
            }

            // 2. Malicious UPDATE attempt -> blocked by trigger
            java.sql.Savepoint sp1 = conn.setSavepoint();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE performance_review_audits SET action = 'TAMPERED' WHERE id = ?")) {
                ps.setLong(1, auditId);
                Exception ex = assertThrows(Exception.class, ps::executeUpdate,
                        "PostgreSQL trigger must reject direct UPDATE on performance_review_audits");
                assertTrue(ex.getMessage().toLowerCase().contains("immutable"),
                        "Expected trigger error mentioning immutable, got: " + ex.getMessage());
            }
            conn.rollback(sp1);

            // 3. Malicious DELETE attempt -> blocked by trigger
            java.sql.Savepoint sp2 = conn.setSavepoint();
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM performance_review_audits WHERE id = ?")) {
                ps.setLong(1, auditId);
                Exception ex = assertThrows(Exception.class, ps::executeUpdate,
                        "PostgreSQL trigger must reject direct DELETE on performance_review_audits");
                assertTrue(ex.getMessage().toLowerCase().contains("immutable"),
                        "Expected trigger error mentioning immutable, got: " + ex.getMessage());
            }
            conn.rollback(sp2);

            conn.rollback();
        }
    }
}
