package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.AttendanceHistoryItemDto;
import com.example.ems.attendance.dto.AttendanceHistoryQuery;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import com.example.ems.security.dto.AuthPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AttendanceHistoryServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private Organization organization;

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
        employee.setEmail("emp@acme.com");
        employee.setStatus("ACTIVE");
        employee.setOrganization(organization);

        AuthPrincipal principal = new AuthPrincipal("125", "sess-1", 1, 100L, "emp@acme.com", "ROLE_EMPLOYEE");
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(employeeRepository.findByEmailAndOrganizationId("emp@acme.com", 100L))
                .thenReturn(Optional.of(employee));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("History: Successfully retrieves paginated history for current employee")
    void testGetHistory_Success() {
        LocalDate date1 = LocalDate.of(2026, 9, 10);
        Attendance a1 = new Attendance();
        a1.setId(101L);
        a1.setDate(date1);
        a1.setStatus(AttendanceStatus.COMPLETED);
        a1.setCheckInTime(Instant.parse("2026-09-10T09:00:00Z"));
        a1.setCheckOutTime(Instant.parse("2026-09-10T18:00:00Z"));
        a1.setTotalBreakMinutes(45);
        a1.setTotalWorkingMinutes(495);

        Page<Attendance> page = new PageImpl<>(List.of(a1));
        when(attendanceRepository.findHistory(eq(125L), eq(100L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        AttendanceHistoryQuery query = new AttendanceHistoryQuery();
        query.setFromDate(LocalDate.of(2026, 9, 1));
        query.setToDate(LocalDate.of(2026, 9, 10));
        query.setStatus(AttendanceStatus.COMPLETED);

        Page<AttendanceHistoryItemDto> result = attendanceService.getAttendanceHistory(query);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        AttendanceHistoryItemDto item = result.getContent().get(0);
        assertEquals(101L, item.getAttendanceId());
        assertEquals("COMPLETED", item.getStatus());
        assertEquals(45, item.getTotalBreakMinutes());
        assertEquals(495, item.getTotalWorkingMinutes());
    }

    @Test
    @DisplayName("History: Rejects if fromDate is after toDate")
    void testGetHistory_InvalidDateRange_ThrowsException() {
        AttendanceHistoryQuery query = new AttendanceHistoryQuery();
        query.setFromDate(LocalDate.of(2026, 9, 15));
        query.setToDate(LocalDate.of(2026, 9, 10));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> attendanceService.getAttendanceHistory(query));
        assertTrue(ex.getMessage().contains("fromDate cannot be after toDate"));
    }

    @Test
    @DisplayName("History: Sort whitelisting falls back to 'date' when illegal column supplied")
    void testSortWhitelisting_Fallback() {
        AttendanceHistoryQuery query = new AttendanceHistoryQuery();
        query.setSortBy("nonExistentColumn; DROP TABLE users;");
        Pageable pageable = query.toPageable();

        assertTrue(pageable.getSort().getOrderFor("date") != null);
        assertNull(pageable.getSort().getOrderFor("nonExistentColumn"));
    }
}
