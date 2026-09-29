package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceCoreResponse;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.*;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.settings.service.SystemSettingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttendanceService core lifecycle methods (Check-In, Breaks, Check-Out, and Queries).
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceCoreServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AttendanceLogService attendanceLogService;

    @Mock
    private SystemSettingService systemSettingService;

    @Mock
    private AttendancePolicyService attendancePolicyService;

    @Spy
    private AttendancePolicyEvaluator attendancePolicyEvaluator = new AttendancePolicyEvaluator();

    @InjectMocks
    private AttendanceService attendanceService;

    private Organization organization;
    private Employee employee;
    private Clock fixedClock;
    private Instant baseInstant;
    private ZoneId zoneId;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Acme Corp");

        employee = new Employee();
        employee.setId(125L);
        employee.setEmployeeId("EMP-125");
        employee.setFullName("John Doe");
        employee.setEmail("john.doe@acme.com");
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);

        zoneId = ZoneOffset.UTC;
        baseInstant = Instant.parse("2026-09-10T09:00:00Z");
        fixedClock = Clock.fixed(baseInstant, zoneId);
        ReflectionTestUtils.setField(attendanceService, "clock", fixedClock);

        // Mock Security Context
        AuthPrincipal principal = new AuthPrincipal("125", "sess-1", 1, 100L, "john.doe@acme.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        com.example.ems.attendance.entity.AttendancePolicy defaultPolicy = new com.example.ems.attendance.entity.AttendancePolicy();
        defaultPolicy.setId(1L);
        defaultPolicy.setName("Default Policy");
        defaultPolicy.setOfficeStartTime(LocalTime.of(9, 0));
        defaultPolicy.setOfficeEndTime(LocalTime.of(18, 0));
        defaultPolicy.setGracePeriodMinutes(15);
        defaultPolicy.setMinimumWorkingMinutes(480);
        defaultPolicy.setHalfDayThreshold(240);
        defaultPolicy.setStatus(com.example.ems.attendance.entity.AttendancePolicyStatus.ACTIVE);
        lenient().when(attendancePolicyService.getActivePolicy(anyLong())).thenReturn(defaultPolicy);

        lenient().when(employeeRepository.findByEmailAndOrganizationId("john.doe@acme.com", 100L))
                .thenReturn(Optional.of(employee));
        lenient().when(systemSettingService.getOfficeStartTime()).thenReturn(LocalTime.of(9, 30));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Check-In: Successfully transitions to WORKING state with server timestamp")
    void testCheckIn_Success() {
        LocalDate today = LocalDate.now(fixedClock);
        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(125L, today, 100L)).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(1001L);
            return a;
        });

        AttendanceCoreResponse response = attendanceService.checkInCore();

        assertNotNull(response);
        assertEquals(1001L, response.getAttendanceId());
        assertEquals(125L, response.getEmployeeId());
        assertEquals("WORKING", response.getStatus());
        assertEquals(baseInstant, response.getCheckInTime());
        assertNull(response.getCheckOutTime());
        assertEquals(0, response.getTotalBreakMinutes());
        assertFalse(response.isActiveBreak());

        verify(attendanceRepository).save(any(Attendance.class));
        verify(attendanceLogService).logSwipe(employee, "SWIPE_IN", "OFFICE_GATE");
    }

    @Test
    @DisplayName("Check-In: Duplicate check-in on same day is rejected with DuplicateCheckInException")
    void testCheckIn_Duplicate_ThrowsException() {
        LocalDate today = LocalDate.now(fixedClock);
        when(attendanceRepository.existsByEmployeeIdAndDateAndOrganizationId(125L, today, 100L)).thenReturn(true);

        DuplicateCheckInException ex = assertThrows(DuplicateCheckInException.class, () -> attendanceService.checkInCore());
        assertTrue(ex.getMessage().contains("Already checked in"));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Start Break: Successfully transitions from WORKING to ON_BREAK")
    void testStartBreak_Success() {
        LocalDate today = LocalDate.now(fixedClock);
        Attendance attendance = new Attendance();
        attendance.setId(1001L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.WORKING);
        attendance.setCheckInTime(baseInstant);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.of(attendance));
        when(attendanceBreakRepository.existsByAttendanceIdAndBreakEndTimeIsNull(1001L)).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(1001L))
                .thenAnswer(inv -> {
                    AttendanceBreak b = new AttendanceBreak(attendance, 100L, baseInstant.plus(Duration.ofHours(4)));
                    b.setId(501L);
                    return List.of(b);
                });

        AttendanceCoreResponse response = attendanceService.startBreakCore();

        assertEquals(AttendanceStatus.ON_BREAK.name(), response.getStatus());
        assertTrue(response.isActiveBreak());
        assertEquals(1, response.getBreaks().size());
        verify(attendanceBreakRepository).save(any(AttendanceBreak.class));
        verify(attendanceLogService).logSwipe(employee, "BREAK_START", "OFFICE_GATE");
    }

    @Test
    @DisplayName("Start Break: Fails if employee has not checked in today")
    void testStartBreak_NoAttendance_ThrowsNotFound() {
        LocalDate today = LocalDate.now(fixedClock);
        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.startBreakCore());
    }

    @Test
    @DisplayName("Start Break: Fails if employee is already on break")
    void testStartBreak_AlreadyOnBreak_ThrowsActiveBreakExists() {
        LocalDate today = LocalDate.now(fixedClock);
        Attendance attendance = new Attendance();
        attendance.setId(1001L);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.of(attendance));

        assertThrows(ActiveBreakExistsException.class, () -> attendanceService.startBreakCore());
    }

    @Test
    @DisplayName("End Break: Successfully transitions from ON_BREAK to WORKING and computes duration")
    void testEndBreak_Success() {
        LocalDate today = LocalDate.now(fixedClock);
        Attendance attendance = new Attendance();
        attendance.setId(1001L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.ON_BREAK);
        attendance.setCheckInTime(baseInstant);

        Instant breakStart = baseInstant.plus(Duration.ofHours(4)); // 13:00
        Instant breakEnd = baseInstant.plus(Duration.ofHours(4).plus(Duration.ofMinutes(30))); // 13:30 (30 mins)
        AttendanceBreak activeBreak = new AttendanceBreak(attendance, 100L, breakStart);
        activeBreak.setId(501L);

        // Advance clock to breakEnd
        Clock breakEndClock = Clock.fixed(breakEnd, zoneId);
        ReflectionTestUtils.setField(attendanceService, "clock", breakEndClock);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.of(attendance));
        when(attendanceBreakRepository.findByAttendanceIdAndBreakEndTimeIsNull(1001L))
                .thenReturn(Optional.of(activeBreak));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(1001L))
                .thenReturn(List.of(activeBreak));

        AttendanceCoreResponse response = attendanceService.endBreakCore();

        assertEquals(AttendanceStatus.WORKING.name(), response.getStatus());
        assertFalse(response.isActiveBreak());
        assertEquals(30, response.getTotalBreakMinutes());
        assertEquals(30, activeBreak.getDurationMinutes());
        assertEquals(breakEnd, activeBreak.getBreakEndTime());
        verify(attendanceLogService).logSwipe(employee, "BREAK_END", "OFFICE_GATE");
    }

    @Test
    @DisplayName("Check-Out: Successfully calculates total duration, breaks, and net working minutes")
    void testCheckOut_SuccessWithMultipleBreaks() {
        LocalDate today = LocalDate.now(fixedClock);
        Attendance attendance = new Attendance();
        attendance.setId(1001L);
        attendance.setEmployee(employee);
        attendance.setOrganization(organization);
        attendance.setDate(today);
        attendance.setStatus(AttendanceStatus.WORKING);
        attendance.setCheckInTime(baseInstant); // 09:00:00

        // Break 1: 13:00 -> 13:30 (30 mins)
        AttendanceBreak b1 = new AttendanceBreak(attendance, 100L, baseInstant.plus(Duration.ofHours(4)));
        b1.setId(501L);
        b1.closeBreak(baseInstant.plus(Duration.ofHours(4).plus(Duration.ofMinutes(30))));

        // Break 2: 16:00 -> 16:15 (15 mins)
        AttendanceBreak b2 = new AttendanceBreak(attendance, 100L, baseInstant.plus(Duration.ofHours(7)));
        b2.setId(502L);
        b2.closeBreak(baseInstant.plus(Duration.ofHours(7).plus(Duration.ofMinutes(15))));

        // Check-Out at 18:00 (9 hours = 540 minutes total elapsed)
        Instant checkOutInstant = baseInstant.plus(Duration.ofHours(9));
        Clock checkOutClock = Clock.fixed(checkOutInstant, zoneId);
        ReflectionTestUtils.setField(attendanceService, "clock", checkOutClock);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.of(attendance));
        when(attendanceBreakRepository.existsByAttendanceIdAndBreakEndTimeIsNull(1001L)).thenReturn(false);
        when(attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(1001L))
                .thenReturn(List.of(b1, b2));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceCoreResponse response = attendanceService.checkOutCore();

        assertEquals(AttendanceStatus.COMPLETED.name(), response.getStatus());
        assertEquals(checkOutInstant, response.getCheckOutTime());
        assertEquals(45, response.getTotalBreakMinutes()); // 30 + 15
        assertEquals(495, response.getTotalWorkingMinutes()); // 540 - 45 = 495 mins (8 hrs 15 mins)
        verify(attendanceLogService).logSwipe(employee, "SWIPE_OUT", "OFFICE_GATE");
    }

    @Test
    @DisplayName("Check-Out: Fails if employee is currently ON_BREAK")
    void testCheckOut_WhileOnBreak_ThrowsInvalidState() {
        LocalDate today = LocalDate.now(fixedClock);
        Attendance attendance = new Attendance();
        attendance.setId(1001L);
        attendance.setStatus(AttendanceStatus.ON_BREAK);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.of(attendance));

        InvalidAttendanceStateException ex = assertThrows(InvalidAttendanceStateException.class, () -> attendanceService.checkOutCore());
        assertTrue(ex.getMessage().contains("currently on break"));
    }

    @Test
    @DisplayName("Get Today: Returns NOT_CHECKED_IN state object when no record exists")
    void testGetTodayAttendance_NotCheckedIn() {
        LocalDate today = LocalDate.now(fixedClock);
        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(125L, today, 100L))
                .thenReturn(Optional.empty());

        AttendanceCoreResponse response = attendanceService.getTodayAttendanceCore();

        assertNotNull(response);
        assertNull(response.getAttendanceId());
        assertEquals("NOT_CHECKED_IN", response.getStatus());
        assertEquals(125L, response.getEmployeeId());
        assertEquals(today, response.getAttendanceDate());
        assertEquals(0, response.getTotalBreakMinutes());
        assertEquals(0, response.getTotalWorkingMinutes());
        assertFalse(response.isActiveBreak());
    }

    @Test
    @DisplayName("Get By ID: Rejects access when attendance belongs to another employee")
    void testGetAttendanceById_OtherEmployee_ThrowsNotFound() {
        when(attendanceRepository.findByIdAndEmployeeIdAndOrganizationId(999L, 125L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(AttendanceNotFoundException.class, () -> attendanceService.getAttendanceByIdCore(999L));
    }

    @Test
    @DisplayName("Inactive employee is rejected on check-in")
    void testCheckIn_InactiveEmployee_ThrowsException() {
        employee.setStatus("INACTIVE");
        assertThrows(EmployeeNotActiveException.class, () -> attendanceService.checkInCore());
    }
}
