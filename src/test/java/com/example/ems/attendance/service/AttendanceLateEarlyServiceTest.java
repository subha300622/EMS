package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.early.EarlyCheckoutQuery;
import com.example.ems.attendance.dto.early.EarlyCheckoutReportDto;
import com.example.ems.attendance.dto.late.LateAttendanceQuery;
import com.example.ems.attendance.dto.late.LateAttendanceReportDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendancePolicy;
import com.example.ems.attendance.entity.AttendancePolicyStatus;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.TeamMemberRepository;
import com.example.ems.employee.repository.TeamRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttendanceLateEarlyServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendancePolicyService attendancePolicyService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private AttendanceLateEarlyService lateEarlyService;

    private Organization organization;
    private Employee employee;
    private AttendancePolicy policy;
    private Attendance lateAttendance;
    private Attendance earlyAttendance;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        TenantContext.setCurrentTenant(100L);

        organization = new Organization();
        organization.setId(100L);

        employee = new Employee();
        employee.setId(101L);
        employee.setFullName("Alice Smith");
        employee.setEmail("alice@example.com");

        policy = new AttendancePolicy();
        policy.setId(1L);
        policy.setName("General Shift");
        policy.setOfficeStartTime(LocalTime.of(9, 0));
        policy.setOfficeEndTime(LocalTime.of(18, 0));
        policy.setGracePeriodMinutes(15);
        policy.setMinimumWorkingMinutes(480);
        policy.setHalfDayThreshold(240);
        policy.setStatus(AttendancePolicyStatus.ACTIVE);

        lateAttendance = new Attendance();
        lateAttendance.setId(201L);
        lateAttendance.setOrganization(organization);
        lateAttendance.setEmployee(employee);
        lateAttendance.setDate(LocalDate.of(2026, 9, 10));
        lateAttendance.setCheckInTime(Instant.parse("2026-09-10T04:00:00Z")); // 09:30 IST (+05:30)
        lateAttendance.setStatus(AttendanceStatus.PRESENT);
        lateAttendance.setIsLate(true);
        lateAttendance.setLateBy("PT30M");
        lateAttendance.setLateByMinutes(30);

        earlyAttendance = new Attendance();
        earlyAttendance.setId(202L);
        earlyAttendance.setOrganization(organization);
        earlyAttendance.setEmployee(employee);
        earlyAttendance.setDate(LocalDate.of(2026, 9, 10));
        earlyAttendance.setCheckInTime(Instant.parse("2026-09-10T03:30:00Z")); // 09:00 IST
        earlyAttendance.setCheckOutTime(Instant.parse("2026-09-10T10:30:00Z")); // 16:00 IST -> 120 mins early
        earlyAttendance.setStatus(AttendanceStatus.PRESENT);
        earlyAttendance.setIsEarlyCheckout(true);
        earlyAttendance.setEarlyBy("PT120M");
        earlyAttendance.setEarlyByMinutes(120);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Get Late Attendance Report: Successfully returns paginated late records")
    void testGetLateAttendanceReport_Success() {
        LateAttendanceQuery query = new LateAttendanceQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));

        when(attendancePolicyService.getActivePolicy(100L)).thenReturn(policy);
        when(attendanceRepository.findLateAttendance(eq(100L), isNull(), isNull(), isNull(), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 10)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lateAttendance)));

        Page<LateAttendanceReportDto> result = lateEarlyService.getLateAttendanceReport(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        LateAttendanceReportDto item = result.getContent().get(0);
        assertEquals(201L, item.getAttendanceId());
        assertEquals(101L, item.getEmployeeId());
        assertEquals("Alice Smith", item.getEmployeeName());
        assertEquals(Integer.valueOf(30), item.getLateByMinutes());
        assertEquals(LocalTime.of(9, 0), item.getExpectedStartTime());
        assertEquals(Integer.valueOf(15), item.getGracePeriodMinutes());
    }

    @Test
    @DisplayName("Get Early Checkout Report: Successfully returns paginated early departure records")
    void testGetEarlyCheckoutReport_Success() {
        EarlyCheckoutQuery query = new EarlyCheckoutQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));

        when(attendancePolicyService.getActivePolicy(100L)).thenReturn(policy);
        when(attendanceRepository.findEarlyCheckoutAttendance(eq(100L), isNull(), isNull(), isNull(), eq(LocalDate.of(2026, 9, 1)), eq(LocalDate.of(2026, 9, 10)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(earlyAttendance)));

        Page<EarlyCheckoutReportDto> result = lateEarlyService.getEarlyCheckoutReport(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        EarlyCheckoutReportDto item = result.getContent().get(0);
        assertEquals(202L, item.getAttendanceId());
        assertEquals(101L, item.getEmployeeId());
        assertEquals("Alice Smith", item.getEmployeeName());
        assertEquals(Integer.valueOf(120), item.getEarlyByMinutes());
        assertEquals(LocalTime.of(18, 0), item.getExpectedEndTime());
    }

    @Test
    @DisplayName("Date Range Validation: Throws IllegalArgumentException if fromDate is after toDate")
    void testDateRangeValidation_Invalid_ThrowsException() {
        LateAttendanceQuery query = new LateAttendanceQuery();
        query.setFromDate(LocalDate.of(2026, 9, 15));
        query.setToDate(LocalDate.of(2026, 9, 10));

        assertThrows(IllegalArgumentException.class, () -> lateEarlyService.getLateAttendanceReport(query));
    }
}
