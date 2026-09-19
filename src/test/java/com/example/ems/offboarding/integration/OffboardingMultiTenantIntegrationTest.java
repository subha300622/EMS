package com.example.ems.offboarding.integration;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.OffboardingAnalyticsResponse;
import com.example.ems.offboarding.dto.OffboardingRequestSummaryDto;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.offboarding.service.OffboardingDashboardService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OffboardingMultiTenantIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private OffboardingDashboardService dashboardService;

    private Organization orgA;
    private Organization orgB;
    private Employee empA;
    private Employee empB;
    private Offboarding offboardingA;
    private Offboarding offboardingB;

    @BeforeEach
    void setUp() {
        // 1. Create Organization A
        orgA = new Organization();
        orgA.setName("Tenant Alpha " + System.currentTimeMillis());
        orgA.setOrganizationCode("ORG_A_" + System.currentTimeMillis());
        orgA = organizationRepository.save(orgA);

        // 2. Create Organization B
        orgB = new Organization();
        orgB.setName("Tenant Beta " + System.currentTimeMillis());
        orgB.setOrganizationCode("ORG_B_" + System.currentTimeMillis());
        orgB = organizationRepository.save(orgB);

        // 3. Create Employee for Org A
        empA = new Employee();
        empA.setFullName("Alice Alpha");
        empA.setEmail("alice.alpha." + System.currentTimeMillis() + "@alpha.com");
        empA.setEmployeeId("EMP_A_" + System.currentTimeMillis());
        empA.setOrganization(orgA);
        empA = employeeRepository.save(empA);

        // 4. Create Employee for Org B
        empB = new Employee();
        empB.setFullName("Bob Beta");
        empB.setEmail("bob.beta." + System.currentTimeMillis() + "@beta.com");
        empB.setEmployeeId("EMP_B_" + System.currentTimeMillis());
        empB.setOrganization(orgB);
        empB = employeeRepository.save(empB);

        // 5. Create Offboarding for Org A
        offboardingA = new Offboarding();
        offboardingA.setEmployee(empA);
        offboardingA.setStatus("IN_PROGRESS");
        offboardingA.setReason("Moving on");
        offboardingA.setReasonCategory("CAREER_GROWTH");
        offboardingA.setResignationDate(LocalDate.now().minusDays(10));
        offboardingA.setRequestedLastWorkingDay(LocalDate.now().plusDays(20)); // SCHEDULED
        offboardingA.setCreatedAt(LocalDateTime.now().minusDays(10));
        offboardingA.setUpdatedAt(LocalDateTime.now().minusDays(10));
        offboardingA = offboardingRepository.save(offboardingA);

        // 6. Create Offboarding for Org B
        offboardingB = new Offboarding();
        offboardingB.setEmployee(empB);
        offboardingB.setStatus("COMPLETED");
        offboardingB.setReason("Contract ended");
        offboardingB.setReasonCategory("CONTRACT_EXPIRATION");
        offboardingB.setResignationDate(LocalDate.now().minusDays(30));
        offboardingB.setRequestedLastWorkingDay(LocalDate.now().minusDays(1));
        offboardingB.setCreatedAt(LocalDateTime.now().minusDays(30));
        offboardingB.setUpdatedAt(LocalDateTime.now().minusDays(1));
        offboardingB = offboardingRepository.save(offboardingB);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Multi-Tenant Requests: Org A only sees Org A offboarding requests")
    void testRequestsIsolation_OrgA() {
        TenantContext.setCurrentTenant(orgA.getId());

        Page<OffboardingRequestSummaryDto> requestsA = dashboardService.getRequests(orgA.getId(), null, null, PageRequest.of(0, 10));

        assertNotNull(requestsA);
        assertEquals(1, requestsA.getTotalElements());
        assertEquals("Alice Alpha", requestsA.getContent().get(0).getEmployeeName());
        assertEquals(offboardingA.getId(), requestsA.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Multi-Tenant Requests: Org B only sees Org B offboarding requests")
    void testRequestsIsolation_OrgB() {
        TenantContext.setCurrentTenant(orgB.getId());

        Page<OffboardingRequestSummaryDto> requestsB = dashboardService.getRequests(orgB.getId(), null, null, PageRequest.of(0, 10));

        assertNotNull(requestsB);
        assertEquals(1, requestsB.getTotalElements());
        assertEquals("Bob Beta", requestsB.getContent().get(0).getEmployeeName());
        assertEquals(offboardingB.getId(), requestsB.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Multi-Tenant Analytics: Metrics are strictly calculated per organization")
    void testAnalyticsIsolation() {
        // 1. Verify Org A analytics
        TenantContext.setCurrentTenant(orgA.getId());
        OffboardingAnalyticsResponse analyticsA = dashboardService.getAnalytics(orgA.getId(), null, null);

        assertNotNull(analyticsA);
        assertEquals(1, analyticsA.getTotalRequests());
        assertEquals(1, analyticsA.getScheduled());
        assertEquals(0, analyticsA.getCompleted());
        assertEquals(1, analyticsA.getVoluntaryExits());

        // 2. Verify Org B analytics
        TenantContext.setCurrentTenant(orgB.getId());
        OffboardingAnalyticsResponse analyticsB = dashboardService.getAnalytics(orgB.getId(), null, null);

        assertNotNull(analyticsB);
        assertEquals(1, analyticsB.getTotalRequests());
        assertEquals(0, analyticsB.getScheduled());
        assertEquals(1, analyticsB.getCompleted());
        assertEquals(1, analyticsB.getVoluntaryExits());
    }
}
