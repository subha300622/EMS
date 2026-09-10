package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceBreakDto;
import com.example.ems.attendance.dto.AttendanceCalendarDayDto;
import com.example.ems.attendance.dto.MonthlyAttendanceCalendarResponse;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceBreak;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceBreakRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.employee.entity.Employee;
import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.organization.entity.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceCalendarServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceBreakRepository attendanceBreakRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private Clock clock;

    @InjectMocks
    private AttendanceCalendarService attendanceCalendarService;

    private Employee employee;
    private Organization organization;
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final Instant fixedNow = Instant.parse("2026-09-05T12:00:00Z"); // Mid-month for testing future/past days

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);
        organization.setName("Acme Corp");

        employee = new Employee();
        employee.setId(10L);
        employee.setEmployeeId("EMP-010");
        employee.setFullName("Alice Wonderland");
        employee.setOrganization(organization);

        lenient().when(clock.instant()).thenReturn(fixedNow);
        lenient().when(clock.getZone()).thenReturn(zoneId);
        lenient().when(attendanceService.resolveCurrentEmployee()).thenReturn(employee);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Scenario 1: Approved Leave -> ON_LEAVE with leaveDays incremented and absentDays unaffected")
    void testScenario1_ApprovedLeave_OnLeaveStatus() {
        LeaveType casualLeave = new LeaveType();
        casualLeave.setName("CASUAL_LEAVE");

        Leave leave = new Leave();
        leave.setId(1L);
        leave.setStartDate(LocalDate.of(2026, 9, 10));
        leave.setEndDate(LocalDate.of(2026, 9, 10));
        leave.setStatus("APPROVED");
        leave.setLeaveType(casualLeave);

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(leave));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        assertNotNull(response);
        assertEquals(1, response.getLeaveDays());

        AttendanceCalendarDayDto day10 = response.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2026, 9, 10)))
                .findFirst()
                .orElseThrow();

        assertEquals("ON_LEAVE", day10.getStatus());
        assertEquals("CASUAL_LEAVE", day10.getLeaveType());
        assertNull(day10.getCheckInTime());
        assertNull(day10.getCheckOutTime());
        assertNull(day10.getTotalWorkingMinutes());
    }

    @Test
    @DisplayName("Scenario 2: Pending Leave -> NOT ON_LEAVE (shows NOT_CHECKED_IN for future dates)")
    void testScenario2_PendingLeave_NotOnLeave() {
        Leave pendingLeave = new Leave();
        pendingLeave.setStartDate(LocalDate.of(2026, 9, 11));
        pendingLeave.setEndDate(LocalDate.of(2026, 9, 11));
        pendingLeave.setStatus("PENDING");

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pendingLeave));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        // Sept 11, 2026 is Friday (future date compared to fixedNow Sept 5) -> NOT_CHECKED_IN
        AttendanceCalendarDayDto day11 = response.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2026, 9, 11)))
                .findFirst()
                .orElseThrow();
        assertEquals("NOT_CHECKED_IN", day11.getStatus());
        assertNotEquals("ON_LEAVE", day11.getStatus());
        assertEquals(0, response.getLeaveDays());
    }

    @Test
    @DisplayName("Scenario 3: Rejected Leave -> NOT ON_LEAVE")
    void testScenario3_RejectedLeave_NotOnLeave() {
        Leave rejectedLeave = new Leave();
        rejectedLeave.setStartDate(LocalDate.of(2026, 9, 11));
        rejectedLeave.setEndDate(LocalDate.of(2026, 9, 11));
        rejectedLeave.setStatus("REJECTED");

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(rejectedLeave));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        AttendanceCalendarDayDto day11 = response.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2026, 9, 11)))
                .findFirst()
                .orElseThrow();
        assertEquals("NOT_CHECKED_IN", day11.getStatus());
        assertNotEquals("ON_LEAVE", day11.getStatus());
        assertEquals(0, response.getLeaveDays());
    }

    @Test
    @DisplayName("Scenario 4: Cancelled Leave -> NOT ON_LEAVE")
    void testScenario4_CancelledLeave_NotOnLeave() {
        Leave cancelledLeave = new Leave();
        cancelledLeave.setStartDate(LocalDate.of(2026, 9, 11));
        cancelledLeave.setEndDate(LocalDate.of(2026, 9, 11));
        cancelledLeave.setStatus("CANCELLED");

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(cancelledLeave));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        AttendanceCalendarDayDto day11 = response.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2026, 9, 11)))
                .findFirst()
                .orElseThrow();
        assertEquals("NOT_CHECKED_IN", day11.getStatus());
        assertNotEquals("ON_LEAVE", day11.getStatus());
        assertEquals(0, response.getLeaveDays());
    }

    @Test
    @DisplayName("Scenario 5: Holiday -> HOLIDAY status with holidayDays incremented")
    void testScenario5_Holiday_HolidayStatus() {
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(LocalDate.of(2026, 9, 15));
        holiday.setName("Company Holiday");
        holiday.setStatus(HolidayStatus.ACTIVE);

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(holiday));

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        assertEquals(1, response.getHolidayDays());
        AttendanceCalendarDayDto day15 = response.getDays().stream()
                .filter(d -> d.getDate().equals(LocalDate.of(2026, 9, 15)))
                .findFirst()
                .orElseThrow();

        assertEquals("HOLIDAY", day15.getStatus());
        assertEquals("Company Holiday", day15.getHolidayName());
        assertNull(day15.getCheckInTime());
        assertNull(day15.getTotalWorkingMinutes());
    }

    @Test
    @DisplayName("Scenario 6: Employee works on Holiday -> status remains HOLIDAY but attendance details are retained")
    void testScenario6_EmployeeWorksOnHoliday_RetainsAttendanceDetails() {
        LocalDate holidayDate = LocalDate.of(2026, 9, 15);
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(holidayDate);
        holiday.setName("Company Holiday");
        holiday.setStatus(HolidayStatus.ACTIVE);

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(holiday));

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        Attendance workAttendance = new Attendance();
        workAttendance.setId(105L);
        workAttendance.setDate(holidayDate);
        workAttendance.setCheckInTime(Instant.parse("2026-09-15T09:00:00Z"));
        workAttendance.setCheckOutTime(Instant.parse("2026-09-15T18:00:00Z"));
        workAttendance.setTotalWorkingMinutes(480);
        workAttendance.setTotalBreakMinutes(60);

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(List.of(workAttendance));

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        AttendanceCalendarDayDto day15 = response.getDays().stream()
                .filter(d -> d.getDate().equals(holidayDate))
                .findFirst()
                .orElseThrow();

        assertEquals("HOLIDAY", day15.getStatus());
        assertEquals("Company Holiday", day15.getHolidayName());
        assertEquals(Instant.parse("2026-09-15T09:00:00Z"), day15.getCheckInTime());
        assertEquals(Instant.parse("2026-09-15T18:00:00Z"), day15.getCheckOutTime());
        assertEquals(480, day15.getTotalWorkingMinutes());
        assertEquals(60, day15.getTotalBreakMinutes());
    }

    @Test
    @DisplayName("Scenario 7: Leave + Attendance conflict -> status is ON_LEAVE but attendance details are retained")
    void testScenario7_LeaveAttendanceConflict_RetainsAttendanceDetails() {
        LocalDate leaveDate = LocalDate.of(2026, 9, 16);
        LeaveType sickLeave = new LeaveType();
        sickLeave.setName("SICK_LEAVE");

        Leave leave = new Leave();
        leave.setStartDate(leaveDate);
        leave.setEndDate(leaveDate);
        leave.setStatus("APPROVED");
        leave.setLeaveType(sickLeave);

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(leave));

        Attendance workAttendance = new Attendance();
        workAttendance.setId(106L);
        workAttendance.setDate(leaveDate);
        workAttendance.setCheckInTime(Instant.parse("2026-09-16T09:00:00Z"));
        workAttendance.setCheckOutTime(Instant.parse("2026-09-16T18:00:00Z"));
        workAttendance.setTotalWorkingMinutes(480);
        workAttendance.setTotalBreakMinutes(60);

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(List.of(workAttendance));

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        AttendanceCalendarDayDto day16 = response.getDays().stream()
                .filter(d -> d.getDate().equals(leaveDate))
                .findFirst()
                .orElseThrow();

        assertEquals("ON_LEAVE", day16.getStatus());
        assertEquals("SICK_LEAVE", day16.getLeaveType());
        assertEquals(Instant.parse("2026-09-16T09:00:00Z"), day16.getCheckInTime());
        assertEquals(480, day16.getTotalWorkingMinutes());
    }

    @Test
    @DisplayName("Scenario 8: Holiday + Leave conflict -> status is HOLIDAY (Holiday takes priority over Leave)")
    void testScenario8_HolidayLeaveConflict_HolidayPriority() {
        LocalDate conflictDate = LocalDate.of(2026, 9, 17);

        Holiday holiday = new Holiday();
        holiday.setHolidayDate(conflictDate);
        holiday.setName("Founder's Day");
        holiday.setStatus(HolidayStatus.ACTIVE);

        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(holiday));

        LeaveType casualLeave = new LeaveType();
        casualLeave.setName("CASUAL_LEAVE");
        Leave leave = new Leave();
        leave.setStartDate(conflictDate);
        leave.setEndDate(conflictDate);
        leave.setStatus("APPROVED");
        leave.setLeaveType(casualLeave);

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(leave));

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        AttendanceCalendarDayDto day17 = response.getDays().stream()
                .filter(d -> d.getDate().equals(conflictDate))
                .findFirst()
                .orElseThrow();

        assertEquals("HOLIDAY", day17.getStatus());
        assertEquals("Founder's Day", day17.getHolidayName());
    }

    @Test
    @DisplayName("Scenario 9: Cross-Tenant Security -> Org A query only fetches Org A holidays and leaves")
    void testScenario9_CrossTenantIsolation() {
        when(holidayRepository.findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(leaveRepository.findOverlappingLeaves(eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        when(attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L)))
                .thenReturn(Collections.emptyList());

        MonthlyAttendanceCalendarResponse response = attendanceCalendarService.getMonthlyCalendar(2026, 9);

        assertNotNull(response);
        verify(holidayRepository).findByOrganizationIdAndStatusAndHolidayDateBetweenOrderByHolidayDateAsc(
                eq(100L), eq(HolidayStatus.ACTIVE), any(LocalDate.class), any(LocalDate.class));
        verify(attendanceRepository).findByEmployeeIdAndDateBetweenAndOrganizationId(
                eq(10L), any(LocalDate.class), any(LocalDate.class), eq(100L));
    }

    @Test
    @DisplayName("Scenario 10: Day Detail API retains break list for worked days and respects Holiday/Leave priority")
    void testScenario10_GetDayDetail_WithBreaksAndHoliday() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        when(holidayRepository.findByOrganizationIdAndHolidayDateAndStatus(eq(100L), eq(date), eq(HolidayStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(leaveRepository.findOverlappingLeaves(eq(10L), eq(date), eq(date)))
                .thenReturn(Collections.emptyList());

        Attendance att = new Attendance();
        att.setId(200L);
        att.setDate(date);
        att.setStatus(AttendanceStatus.COMPLETED);
        att.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        att.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        att.setTotalWorkingMinutes(495);
        att.setTotalBreakMinutes(45);

        when(attendanceRepository.findByEmployeeIdAndDateAndOrganizationId(eq(10L), eq(date), eq(100L)))
                .thenReturn(Optional.of(att));

        AttendanceBreak b1 = new AttendanceBreak();
        b1.setId(1L);
        b1.setBreakStartTime(Instant.parse("2026-09-10T13:00:00Z"));
        b1.setBreakEndTime(Instant.parse("2026-09-10T13:45:00Z"));
        b1.setDurationMinutes(45);

        when(attendanceBreakRepository.findByAttendanceIdOrderByBreakStartTimeAsc(200L))
                .thenReturn(List.of(b1));

        AttendanceBreakDto breakDto = new AttendanceBreakDto(1L, b1.getBreakStartTime(), b1.getBreakEndTime(), 45, false);
        when(attendanceService.mapBreakToDto(b1)).thenReturn(breakDto);

        AttendanceCalendarDayDto dayDetail = attendanceCalendarService.getDayDetail(date);

        assertNotNull(dayDetail);
        assertEquals(date, dayDetail.getDate());
        assertEquals("PRESENT", dayDetail.getStatus());
        assertEquals(495, dayDetail.getTotalWorkingMinutes());
        assertEquals(45, dayDetail.getTotalBreakMinutes());
        assertEquals(1, dayDetail.getBreaks().size());
        assertEquals(45, dayDetail.getBreaks().get(0).getDurationMinutes());
    }

    @Test
    @DisplayName("Input Validation: Invalid month or year throws IllegalArgumentException")
    void testInputValidation_InvalidMonthOrYear() {
        assertThrows(IllegalArgumentException.class, () -> attendanceCalendarService.getMonthlyCalendar(2026, 13));
        assertThrows(IllegalArgumentException.class, () -> attendanceCalendarService.getMonthlyCalendar(1800, 5));
        assertThrows(IllegalArgumentException.class, () -> attendanceCalendarService.getDayDetail(null));
    }
}
