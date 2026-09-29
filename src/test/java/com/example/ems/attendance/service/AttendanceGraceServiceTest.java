package com.example.ems.attendance.service;

import com.example.ems.attendance.entity.AttendanceGraceUsage;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.GracePeriodType;
import com.example.ems.attendance.repository.AttendanceGraceUsageRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceGraceServiceTest {

    @Mock
    private AttendanceGraceUsageRepository graceUsageRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceGraceService graceService;

    private AttendancePolicy policy;
    private Organization organization;
    private Employee employee;

    @BeforeEach
    void setUp() {
        organization = new Organization();
        organization.setId(1L);

        employee = new Employee();
        employee.setId(10L);
        employee.setOrganization(organization);

        policy = new AttendancePolicy();
        policy.setId(100L);
        policy.setName("Test Policy");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setLateGraceMinutes(10);
        policy.setEarlyExitGraceMinutes(10);
        policy.setGraceOccurrencesPerPeriod(3);
        policy.setGracePeriodType(GracePeriodType.MONTHLY);
        policy.setAllowLateGrace(true);
        policy.setAllowEarlyExitGrace(true);
    }

    @Test
    @DisplayName("Period bounds calculation for MONTHLY")
    void testCalculatePeriodBounds_Monthly() {
        LocalDate date = LocalDate.of(2026, 9, 15);
        AttendanceGraceService.PeriodBounds bounds = graceService.calculatePeriodBounds(date, GracePeriodType.MONTHLY);

        assertEquals(LocalDate.of(2026, 9, 1), bounds.startDate());
        assertEquals(LocalDate.of(2026, 9, 30), bounds.endDate());
    }

    @Test
    @DisplayName("Evaluate Late Grace: Arrived 7 minutes late with 0 usages -> Grace applied")
    void testEvaluateLateGrace_WithinGrace() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        when(graceUsageRepository.countGraceUsagesInPeriod(eq(1L), eq(10L), eq("LATE_ARRIVAL"), any(), any()))
                .thenReturn(0L);

        AttendanceGraceService.GraceEvaluationResult result =
                graceService.evaluateLateGrace(1L, 10L, date, 7, policy);

        assertTrue(result.graceApplied());
        assertEquals(7, result.graceMinutes());
        assertFalse(result.limitExceeded());
        assertEquals(1, result.occurrencesUsedInPeriod());
    }

    @Test
    @DisplayName("Evaluate Late Grace: Arrived 25 minutes late (> 10 min grace) -> Grace not applied")
    void testEvaluateLateGrace_ExceedsGraceMinutes() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        AttendanceGraceService.GraceEvaluationResult result =
                graceService.evaluateLateGrace(1L, 10L, date, 25, policy);

        assertFalse(result.graceApplied());
        assertEquals(0, result.graceMinutes());
        assertFalse(result.limitExceeded());
    }

    @Test
    @DisplayName("Evaluate Late Grace: Arrived 7 minutes late but already used 3/3 grace occurrences -> Limit exceeded")
    void testEvaluateLateGrace_OccurrencesExhausted() {
        LocalDate date = LocalDate.of(2026, 9, 25);
        when(graceUsageRepository.countGraceUsagesInPeriod(eq(1L), eq(10L), eq("LATE_ARRIVAL"), any(), any()))
                .thenReturn(3L);

        AttendanceGraceService.GraceEvaluationResult result =
                graceService.evaluateLateGrace(1L, 10L, date, 7, policy);

        assertFalse(result.graceApplied());
        assertTrue(result.limitExceeded());
        assertEquals(3, result.occurrencesUsedInPeriod());
    }

    @Test
    @DisplayName("Record Grace Usage persists new ledger record")
    void testRecordGraceUsage_NewRecord() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        when(graceUsageRepository.findByOrgEmpDateAndType(1L, 10L, date, "LATE_ARRIVAL")).thenReturn(Optional.empty());
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(graceUsageRepository.save(any(AttendanceGraceUsage.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceGraceUsage usage = graceService.recordGraceUsage(1L, 10L, date, "LATE_ARRIVAL", 7, true, policy);

        assertNotNull(usage);
        assertEquals(7, usage.getGraceMinutesUsed());
        assertTrue(usage.getWithinGrace());
        assertEquals("LATE_ARRIVAL", usage.getGraceType());
        verify(graceUsageRepository).save(any(AttendanceGraceUsage.class));
    }
}
