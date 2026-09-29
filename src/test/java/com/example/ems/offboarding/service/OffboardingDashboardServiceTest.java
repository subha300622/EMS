package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.offboarding.dto.OffboardingAnalyticsResponse;
import com.example.ems.offboarding.dto.OffboardingRequestSummaryDto;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffboardingDashboardServiceTest {

    @Mock
    private OffboardingRepository offboardingRepository;

    @InjectMocks
    private OffboardingDashboardService dashboardService;

    private Organization testOrg;
    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        testOrg.setId(100L);
        testOrg.setName("Acme Corp");

        employee1 = new Employee();
        employee1.setId(1L);
        employee1.setFullName("Alice Smith");
        employee1.setEmployeeId("EMP001");
        employee1.setDepartment("Engineering");
        employee1.setDesignation("Software Engineer");
        employee1.setOrganization(testOrg);

        employee2 = new Employee();
        employee2.setId(2L);
        employee2.setFullName("Bob Jones");
        employee2.setEmployeeId("EMP002");
        employee2.setDepartment("Finance");
        employee2.setDesignation("Financial Analyst");
        employee2.setOrganization(testOrg);
    }

    @Test
    @DisplayName("Status Mapping: COMPLETED precedence")
    void testResolveDashboardStatus_Completed() {
        Offboarding ob = new Offboarding();
        ob.setStatus("COMPLETED");
        ob.setRequestedLastWorkingDay(LocalDate.now().plusDays(10)); // Future date should still be COMPLETED

        assertEquals("COMPLETED", dashboardService.resolveDashboardStatus(ob));
    }

    @Test
    @DisplayName("Status Mapping: SCHEDULED for future exit date when not completed")
    void testResolveDashboardStatus_Scheduled() {
        Offboarding ob = new Offboarding();
        ob.setStatus("APPROVED");
        ob.setRequestedLastWorkingDay(LocalDate.now().plusDays(14));

        assertEquals("SCHEDULED", dashboardService.resolveDashboardStatus(ob));
    }

    @Test
    @DisplayName("Status Mapping: ACTIVE for pending or in-progress past/current exit date")
    void testResolveDashboardStatus_Active() {
        Offboarding ob = new Offboarding();
        ob.setStatus("IN_PROGRESS");
        ob.setRequestedLastWorkingDay(LocalDate.now().minusDays(2));

        assertEquals("ACTIVE", dashboardService.resolveDashboardStatus(ob));
    }

    @Test
    @DisplayName("Get Requests: With status filter ACTIVE")
    void testGetRequests_StatusFilter() {
        Offboarding ob1 = new Offboarding();
        ob1.setId(10L);
        ob1.setEmployee(employee1);
        ob1.setStatus("IN_PROGRESS");
        ob1.setRequestedLastWorkingDay(LocalDate.now().minusDays(1));
        Offboarding ob2 = new Offboarding();
        ob2.setId(20L);
        ob2.setEmployee(employee2);
        ob2.setStatus("COMPLETED");

        when(offboardingRepository.findAll(ArgumentMatchers.<Specification<Offboarding>>any())).thenReturn(List.of(ob1, ob2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<OffboardingRequestSummaryDto> result = dashboardService.getRequests(100L, "ACTIVE", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Alice Smith", result.getContent().get(0).getEmployeeName());
        assertEquals("ACTIVE", result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Analytics: Empty organization returns zeroes")
    void testGetAnalytics_EmptyOrg() {
        when(offboardingRepository.findByEmployeeOrganizationId(100L)).thenReturn(Collections.emptyList());

        OffboardingAnalyticsResponse analytics = dashboardService.getAnalytics(100L, null, null);

        assertNotNull(analytics);
        assertEquals(0, analytics.getTotalRequests());
        assertEquals(0, analytics.getActive());
        assertEquals(0, analytics.getCompleted());
        assertEquals(0, analytics.getScheduled());
        assertEquals(0.0, analytics.getAverageNoticePeriodDays());
        assertEquals(0.0, analytics.getAverageExitCompletionDays());
    }

    @Test
    @DisplayName("Analytics: Correct counts and averages calculation")
    void testGetAnalytics_Calculations() {
        Offboarding ob1 = new Offboarding();
        ob1.setId(1L);
        ob1.setEmployee(employee1);
        ob1.setStatus("COMPLETED");
        ob1.setReasonCategory("CAREER_GROWTH");
        ob1.setResignationDate(LocalDate.now().minusDays(30));
        ob1.setRequestedLastWorkingDay(LocalDate.now().minusDays(0));
        ob1.setCreatedAt(LocalDateTime.now().minusDays(30));
        ob1.setUpdatedAt(LocalDateTime.now().minusDays(2));

        Offboarding ob2 = new Offboarding();
        ob2.setId(2L);
        ob2.setEmployee(employee2);
        ob2.setStatus("APPROVED");
        ob2.setReasonCategory("INVOLUNTARY_TERMINATION");
        ob2.setResignationDate(LocalDate.now().minusDays(5));
        ob2.setRequestedLastWorkingDay(LocalDate.now().plusDays(25)); // SCHEDULED
        ob2.setCreatedAt(LocalDateTime.now().minusDays(5));
        ob2.setUpdatedAt(LocalDateTime.now().minusDays(5));

        when(offboardingRepository.findByEmployeeOrganizationId(100L)).thenReturn(List.of(ob1, ob2));

        OffboardingAnalyticsResponse analytics = dashboardService.getAnalytics(100L, null, null);

        assertNotNull(analytics);
        assertEquals(2, analytics.getTotalRequests());
        assertEquals(1, analytics.getCompleted());
        assertEquals(1, analytics.getScheduled());
        assertEquals(0, analytics.getActive());
        assertEquals(1, analytics.getVoluntaryExits());
        assertEquals(1, analytics.getInvoluntaryExits());
        assertEquals(30.0, analytics.getAverageNoticePeriodDays());
        assertEquals(28.0, analytics.getAverageExitCompletionDays());
    }
}
