package com.example.ems.superadmin.dashboard.service.query;

import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.superadmin.dashboard.dto.SuperAdminDashboardResponse.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SuperAdminDashboardQueryUnitTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> longQuery;

    @Mock
    private TypedQuery<BigDecimal> bigDecimalQuery;

    @Mock
    private TypedQuery<Object[]> objectArrayQuery;

    @Mock
    private Query nativeQuery;

    private OrganizationDashboardQuery organizationQuery;
    private EmployeeDashboardQuery employeeQuery;
    private SubscriptionDashboardQuery subscriptionQuery;
    private RevenueDashboardQuery revenueQuery;
    private AttendanceDashboardQuery attendanceQuery;
    private LeaveDashboardQuery leaveQuery;
    private PayrollDashboardQuery payrollQuery;
    private ApprovalDashboardQuery approvalQuery;
    private SupportDashboardQuery supportQuery;
    private AuditDashboardQuery auditQuery;

    @BeforeEach
    void setUp() {
        organizationQuery = new OrganizationDashboardQuery();
        ReflectionTestUtils.setField(organizationQuery, "entityManager", entityManager);

        employeeQuery = new EmployeeDashboardQuery();
        ReflectionTestUtils.setField(employeeQuery, "entityManager", entityManager);

        subscriptionQuery = new SubscriptionDashboardQuery();
        ReflectionTestUtils.setField(subscriptionQuery, "entityManager", entityManager);

        revenueQuery = new RevenueDashboardQuery();
        ReflectionTestUtils.setField(revenueQuery, "entityManager", entityManager);

        attendanceQuery = new AttendanceDashboardQuery();
        ReflectionTestUtils.setField(attendanceQuery, "entityManager", entityManager);

        leaveQuery = new LeaveDashboardQuery();
        ReflectionTestUtils.setField(leaveQuery, "entityManager", entityManager);

        payrollQuery = new PayrollDashboardQuery();
        ReflectionTestUtils.setField(payrollQuery, "entityManager", entityManager);

        approvalQuery = new ApprovalDashboardQuery();
        ReflectionTestUtils.setField(approvalQuery, "entityManager", entityManager);

        supportQuery = new SupportDashboardQuery();
        ReflectionTestUtils.setField(supportQuery, "entityManager", entityManager);

        auditQuery = new AuditDashboardQuery();
        ReflectionTestUtils.setField(auditQuery, "entityManager", entityManager);
    }

    // ── 1. OrganizationDashboardQuery ──────────────────────────────────────────

    @Test
    void testOrganizationCounts_calculation() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);

        // Sequence: total (100), active (80), suspended (5), newCount (10), prevNewCount (5)
        when(longQuery.getSingleResult()).thenReturn(100L, 80L, 5L, 10L, 5L);

        OrganizationDashboardQuery.OrganizationCounts counts = organizationQuery.queryCounts(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        );

        assertThat(counts.total()).isEqualTo(100L);
        assertThat(counts.active()).isEqualTo(80L);
        assertThat(counts.inactive()).isEqualTo(20L); // 100 - 80
        assertThat(counts.newCount()).isEqualTo(10L);
        assertThat(counts.suspended()).isEqualTo(5L);
        assertThat(counts.activated()).isEqualTo(10L); // min(10, 80)
        assertThat(counts.growthPercentage()).isEqualTo(100.0); // (10-5)/5 * 100
    }

    @Test
    void testOrganizationCounts_zeroPreviousNew() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(10L, 8L, 0L, 5L, 0L);

        OrganizationDashboardQuery.OrganizationCounts counts = organizationQuery.queryCounts(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        );

        assertThat(counts.growthPercentage()).isEqualTo(100.0);
    }

    @Test
    void testOrganizationRecentOrganizations() {
        when(entityManager.createQuery(anyString())).thenReturn(objectArrayQuery);
        when(objectArrayQuery.setMaxResults(anyInt())).thenReturn(objectArrayQuery);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{101L, "Org Alpha", OrganizationStatus.ACTIVE, 50L, Instant.now()});
        when(objectArrayQuery.getResultList()).thenReturn(rows);

        List<RecentOrganizationDto> recent = organizationQuery.queryRecentOrganizations(5);
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).organizationId()).isEqualTo(101L);
        assertThat(recent.get(0).organizationName()).isEqualTo("Org Alpha");
        assertThat(recent.get(0).status()).isEqualTo("ACTIVE");
        assertThat(recent.get(0).employeeCount()).isEqualTo(50L);
    }

    @Test
    void testOrganizationGrowthChart_exceptionHandled() {
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("DB error"));
        List<ChartPointDto> chart = organizationQuery.queryGrowthChart();
        assertThat(chart).isEmpty();
    }

    // ── 2. EmployeeDashboardQuery ──────────────────────────────────────────────

    @Test
    void testEmployeeCounts_calculation() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);

        // Sequence: total (500), active (450), newCount (25), prevNewCount (20), exitedCount (5)
        when(longQuery.getSingleResult()).thenReturn(500L, 450L, 25L, 20L, 5L);

        EmployeeDashboardQuery.EmployeeCounts counts = employeeQuery.queryCounts(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        );

        assertThat(counts.total()).isEqualTo(500L);
        assertThat(counts.active()).isEqualTo(450L);
        assertThat(counts.inactive()).isEqualTo(50L);
        assertThat(counts.newEmployees()).isEqualTo(25L);
        assertThat(counts.exitedEmployees()).isEqualTo(5L);
        assertThat(counts.growthPercentage()).isEqualTo(25.0); // (25-20)/20 * 100
    }

    @Test
    void testEmployeeGrowthChart_success() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"2026-09", 15L});
        when(nativeQuery.getResultList()).thenReturn(rows);

        List<ChartPointDto> chart = employeeQuery.queryGrowthChart();
        assertThat(chart).hasSize(1);
        assertThat(chart.get(0).period()).isEqualTo("2026-09");
        assertThat(chart.get(0).count()).isEqualTo(15L);
    }

    // ── 3. SubscriptionDashboardQuery ──────────────────────────────────────────

    @Test
    void testSubscriptionCounts() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);

        // Sequence: total (50), active (40), trial (5), expired (3), cancelled (2), expiringSoon (4)
        when(longQuery.getSingleResult()).thenReturn(50L, 40L, 5L, 3L, 2L, 4L);

        SubscriptionDashboardQuery.SubscriptionCounts counts = subscriptionQuery.queryCounts();
        assertThat(counts.total()).isEqualTo(50L);
        assertThat(counts.active()).isEqualTo(40L);
        assertThat(counts.trial()).isEqualTo(5L);
        assertThat(counts.expired()).isEqualTo(3L);
        assertThat(counts.cancelled()).isEqualTo(2L);
        assertThat(counts.expiringSoon()).isEqualTo(4L);
    }

    // ── 4. RevenueDashboardQuery ───────────────────────────────────────────────

    @Test
    void testRevenueQuery_withGrowth() {
        when(entityManager.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(bigDecimalQuery);
        when(bigDecimalQuery.setParameter(anyString(), any())).thenReturn(bigDecimalQuery);
        when(bigDecimalQuery.getSingleResult()).thenReturn(new BigDecimal("120000.00"), new BigDecimal("100000.00"));

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(45L);

        RevenueDashboardQuery.RevenueStatsResult rev = revenueQuery.queryRevenue(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        );

        assertThat(rev.currentPeriod()).isEqualTo(new BigDecimal("120000.00"));
        assertThat(rev.previousPeriod()).isEqualTo(new BigDecimal("100000.00"));
        assertThat(rev.growthPercentage()).isEqualTo(20.0);
        assertThat(rev.currency()).isEqualTo("INR");
        assertThat(rev.transactions()).isEqualTo(45L);
    }

    @Test
    void testRevenueTrend_success() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"2026-09", new BigDecimal("500000.00")});
        when(nativeQuery.getResultList()).thenReturn(rows);

        List<RevenueChartDataPoint> trend = revenueQuery.queryRevenueTrend();
        assertThat(trend).hasSize(1);
        assertThat(trend.get(0).period()).isEqualTo("2026-09");
        assertThat(trend.get(0).revenue()).isEqualTo(new BigDecimal("500000.00"));
    }

    // ── 5. AttendanceDashboardQuery ────────────────────────────────────────────

    @Test
    void testAttendanceQuery_percentageCalculation() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);

        // Sequence: present (90), absent (5), late (5), onLeave (10)
        // applicable = 90 + 5 + 5 = 100, attendance % = 90 / 100 = 90.0%
        when(longQuery.getSingleResult()).thenReturn(90L, 5L, 5L, 10L);

        AttendanceStats stats = attendanceQuery.queryAttendance(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(stats.present()).isEqualTo(90L);
        assertThat(stats.absent()).isEqualTo(5L);
        assertThat(stats.late()).isEqualTo(5L);
        assertThat(stats.onLeave()).isEqualTo(10L);
        assertThat(stats.attendancePercentage()).isEqualTo(90.0);
    }

    // ── 6. LeaveDashboardQuery ─────────────────────────────────────────────────

    @Test
    void testLeaveQuery() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);

        // Sequence: pending (12), approved (50), rejected (4), cancelled (3)
        when(longQuery.getSingleResult()).thenReturn(12L, 50L, 4L, 3L);

        LeaveStats stats = leaveQuery.queryLeave(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(stats.pending()).isEqualTo(12L);
        assertThat(stats.approved()).isEqualTo(50L);
        assertThat(stats.rejected()).isEqualTo(4L);
        assertThat(stats.cancelled()).isEqualTo(3L);
    }

    // ── 7. PayrollDashboardQuery ───────────────────────────────────────────────

    @Test
    void testPayrollQuery() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(3L, 25L, 1L);

        when(entityManager.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(bigDecimalQuery);
        when(bigDecimalQuery.setParameter(anyString(), any())).thenReturn(bigDecimalQuery);
        when(bigDecimalQuery.getSingleResult()).thenReturn(new BigDecimal("7500000.00"));

        PayrollStats stats = payrollQuery.queryPayroll();
        assertThat(stats.pending()).isEqualTo(3L);
        assertThat(stats.processed()).isEqualTo(25L);
        assertThat(stats.failed()).isEqualTo(1L);
        assertThat(stats.totalPayroll()).isEqualTo(new BigDecimal("7500000.00"));
        assertThat(stats.currency()).isEqualTo("INR");
    }

    // ── 8. ApprovalDashboardQuery ──────────────────────────────────────────────

    @Test
    void testApprovalQuery() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(14L);

        long count = approvalQuery.queryPendingApprovals();
        assertThat(count).isEqualTo(14L);
    }

    // ── 9. SupportDashboardQuery ───────────────────────────────────────────────

    @Test
    void testSupportQuery() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter(anyString(), any())).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(8L);

        long count = supportQuery.queryPendingSupportTickets();
        assertThat(count).isEqualTo(8L);
    }

    // ── 10. AuditDashboardQuery ────────────────────────────────────────────────

    @Test
    void testAuditUserCounts() {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(200L, 185L);

        AuditDashboardQuery.UserCounts counts = auditQuery.queryUserCounts();
        assertThat(counts.totalUsers()).isEqualTo(200L);
        assertThat(counts.activeUsers()).isEqualTo(185L);
    }

    @Test
    void testAuditRecentUsers() {
        when(entityManager.createQuery(anyString())).thenReturn(objectArrayQuery);
        when(objectArrayQuery.setMaxResults(anyInt())).thenReturn(objectArrayQuery);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{501L, "Alice Smith", "alice@example.com", "Acme Corp", "ACTIVE", Instant.now()});
        when(objectArrayQuery.getResultList()).thenReturn(rows);

        List<RecentUserDto> users = auditQuery.queryRecentUsers(5);
        assertThat(users).hasSize(1);
        assertThat(users.get(0).userId()).isEqualTo(501L);
        assertThat(users.get(0).name()).isEqualTo("Alice Smith");
        assertThat(users.get(0).email()).isEqualTo("alice@example.com");
        assertThat(users.get(0).organizationName()).isEqualTo("Acme Corp");
    }

    @Test
    void testAuditRecentActivities() {
        when(entityManager.createQuery(anyString())).thenReturn(objectArrayQuery);
        when(objectArrayQuery.setMaxResults(anyInt())).thenReturn(objectArrayQuery);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{999L, "USER_LOGIN", "AUTH", "501", "USR_1", 10L, "SUCCESS", LocalDateTime.now()});
        when(objectArrayQuery.getResultList()).thenReturn(rows);

        List<RecentActivityDto> acts = auditQuery.queryRecentActivities(10);
        assertThat(acts).hasSize(1);
        assertThat(acts.get(0).id()).isEqualTo(999L);
        assertThat(acts.get(0).action()).isEqualTo("USER_LOGIN");
        assertThat(acts.get(0).resource()).isEqualTo("AUTH");
        assertThat(acts.get(0).status()).isEqualTo("SUCCESS");
    }
}
