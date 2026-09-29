package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.DepartmentDailyAttendanceResponse;
import com.example.ems.attendance.dto.DepartmentMemberDailyAttendanceDto;
import com.example.ems.attendance.dto.TeamDailyAttendanceResponse;
import com.example.ems.attendance.dto.TeamDepartmentAttendanceHistoryItemDto;
import com.example.ems.attendance.dto.TeamMemberDailyAttendanceDto;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.exception.AttendanceNotFoundException;
import com.example.ems.attendance.service.DepartmentAttendanceService;
import com.example.ems.attendance.service.TeamAttendanceService;
import com.example.ems.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AttendanceTeamDepartmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamAttendanceService teamAttendanceService;

    @Mock
    private DepartmentAttendanceService departmentAttendanceService;

    @InjectMocks
    private AttendanceTeamDepartmentController attendanceTeamDepartmentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceTeamDepartmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // ── Team Attendance Endpoints ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/attendance/teams/{teamId} - returns 200 with daily team attendance")
    void testGetTeamDailyAttendance_Success() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 10);
        TeamDailyAttendanceResponse response = new TeamDailyAttendanceResponse();
        response.setTeamId(12L);
        response.setTeamName("Backend Core");
        response.setTeamCode("BE-CORE");
        response.setDate(date);
        response.setTotalEmployees(10);
        response.setPresentCount(8);
        response.setAbsentCount(1);
        response.setOnLeaveCount(1);
        response.setLateCount(2);

        TeamMemberDailyAttendanceDto member = new TeamMemberDailyAttendanceDto();
        member.setEmployeeId(1L);
        member.setEmployeeCode("EMP-001");
        member.setFullName("John Doe");
        member.setStatus("PRESENT");
        response.setMembers(List.of(member));

        when(teamAttendanceService.getTeamDailyAttendance(eq(12L), eq(date), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/teams/12")
                        .param("date", "2026-09-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teamId").value(12))
                .andExpect(jsonPath("$.data.teamName").value("Backend Core"))
                .andExpect(jsonPath("$.data.totalEmployees").value(10))
                .andExpect(jsonPath("$.data.presentCount").value(8))
                .andExpect(jsonPath("$.data.members[0].fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/teams/{teamId} - returns 404 when team not found or cross-tenant")
    void testGetTeamDailyAttendance_NotFound_Returns404() throws Exception {
        when(teamAttendanceService.getTeamDailyAttendance(eq(999L), any(), any()))
                .thenThrow(new AttendanceNotFoundException("Team not found with ID: 999 within the current organization."));

        mockMvc.perform(get("/api/v1/attendance/teams/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("ATT_404"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/teams/{teamId}/history - returns 200 with paginated history")
    void testGetTeamAttendanceHistory_Success() throws Exception {
        TeamDepartmentAttendanceHistoryItemDto item = new TeamDepartmentAttendanceHistoryItemDto();
        item.setAttendanceId(101L);
        item.setEmployeeId(1L);
        item.setEmployeeCode("EMP-001");
        item.setEmployeeName("John Doe");
        item.setStatus("COMPLETED");
        item.setTotalWorkingMinutes(480);

        Page<TeamDepartmentAttendanceHistoryItemDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(teamAttendanceService.getTeamAttendanceHistory(eq(12L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/attendance/teams/12/history")
                        .param("fromDate", "2026-09-01")
                        .param("toDate", "2026-09-10")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].attendanceId").value(101))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/teams/{teamId}/history - returns 400 on invalid date range (fromDate > toDate)")
    void testGetTeamAttendanceHistory_InvalidDateRange_Returns400() throws Exception {
        when(teamAttendanceService.getTeamAttendanceHistory(eq(12L), any()))
                .thenThrow(new IllegalArgumentException("fromDate cannot be after toDate"));

        mockMvc.perform(get("/api/v1/attendance/teams/12/history")
                        .param("fromDate", "2026-09-20")
                        .param("toDate", "2026-09-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    // ── Department Attendance Endpoints ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/attendance/departments/{departmentId} - returns 200 with daily department attendance")
    void testGetDepartmentDailyAttendance_Success() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 10);
        DepartmentDailyAttendanceResponse response = new DepartmentDailyAttendanceResponse();
        response.setDepartmentId(5L);
        response.setDepartmentName("Engineering");
        response.setDate(date);
        response.setTotalEmployees(30);
        response.setPresentCount(28);
        response.setAbsentCount(2);

        when(departmentAttendanceService.getDepartmentDailyAttendance(eq(5L), eq(date), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/departments/5")
                        .param("date", "2026-09-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(5))
                .andExpect(jsonPath("$.data.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.data.totalEmployees").value(30))
                .andExpect(jsonPath("$.data.presentCount").value(28));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/departments/{departmentId} - returns 404 when department not found or cross-tenant")
    void testGetDepartmentDailyAttendance_NotFound_Returns404() throws Exception {
        when(departmentAttendanceService.getDepartmentDailyAttendance(eq(999L), any(), any()))
                .thenThrow(new AttendanceNotFoundException("Department not found with ID: 999 within the current organization."));

        mockMvc.perform(get("/api/v1/attendance/departments/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("ATT_404"));
    }

    @Test
    @DisplayName("GET /api/v1/attendance/departments/{departmentId}/history - returns 200 with paginated history")
    void testGetDepartmentAttendanceHistory_Success() throws Exception {
        TeamDepartmentAttendanceHistoryItemDto item = new TeamDepartmentAttendanceHistoryItemDto();
        item.setAttendanceId(202L);
        item.setEmployeeId(2L);
        item.setEmployeeCode("EMP-002");
        item.setEmployeeName("Jane Smith");
        item.setDepartmentName("Engineering");
        item.setStatus("COMPLETED");

        Page<TeamDepartmentAttendanceHistoryItemDto> page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);
        when(departmentAttendanceService.getDepartmentAttendanceHistory(eq(5L), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/attendance/departments/5/history")
                        .param("fromDate", "2026-09-01")
                        .param("toDate", "2026-09-10")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].attendanceId").value(202))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.data.content[0].departmentName").value("Engineering"));
    }

    @Test
    @DisplayName("Test 2: GET /api/v1/attendance/teams/{teamId}?status=PRESENT - returns filtered members")
    void testGetTeamDailyAttendance_StatusFilter_ReturnsFilteredMembers() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 10);
        TeamDailyAttendanceResponse response = new TeamDailyAttendanceResponse();
        response.setTeamId(12L);
        response.setTeamName("Backend Core");
        response.setDate(date);
        response.setTotalEmployees(10);
        response.setPresentCount(1);

        TeamMemberDailyAttendanceDto member = new TeamMemberDailyAttendanceDto();
        member.setEmployeeId(1L);
        member.setEmployeeCode("EMP-001");
        member.setFullName("John Doe");
        member.setStatus("PRESENT");
        response.setMembers(List.of(member));

        when(teamAttendanceService.getTeamDailyAttendance(eq(12L), eq(date), eq(AttendanceStatus.PRESENT)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/teams/12")
                        .param("date", "2026-09-10")
                        .param("status", "PRESENT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.members[0].status").value("PRESENT"));
    }

    @Test
    @DisplayName("Test 8: GET /api/v1/attendance/teams/{teamId}/history - returns 400 on unwhitelisted sortBy")
    void testGetTeamAttendanceHistory_InvalidSort_Returns400() throws Exception {
        when(teamAttendanceService.getTeamAttendanceHistory(eq(12L), any()))
                .thenThrow(new IllegalArgumentException("Invalid sort field 'employeeName'. Allowed fields: [date, checkInTime, checkOutTime, status]"));

        mockMvc.perform(get("/api/v1/attendance/teams/12/history")
                        .param("sortBy", "employeeName")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("Test 10: GET /api/v1/attendance/departments/{departmentId}?status=ON_LEAVE - returns filtered members")
    void testGetDepartmentDailyAttendance_StatusFilter_ReturnsFilteredMembers() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 10);
        DepartmentDailyAttendanceResponse response = new DepartmentDailyAttendanceResponse();
        response.setDepartmentId(5L);
        response.setDepartmentName("Engineering");
        response.setDate(date);
        response.setTotalEmployees(30);
        response.setOnLeaveCount(1);

        DepartmentMemberDailyAttendanceDto member = new DepartmentMemberDailyAttendanceDto();
        member.setEmployeeId(2L);
        member.setEmployeeCode("EMP-002");
        member.setFullName("Jane Smith");
        member.setStatus("ON_LEAVE");
        response.setMembers(List.of(member));

        when(departmentAttendanceService.getDepartmentDailyAttendance(eq(5L), eq(date), eq(AttendanceStatus.LEAVE)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/attendance/departments/5")
                        .param("date", "2026-09-10")
                        .param("status", "LEAVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.members[0].status").value("ON_LEAVE"));
    }

    @Test
    @DisplayName("Test 12: GET /api/v1/attendance/departments/{departmentId}/history - returns 400 on unwhitelisted sortBy")
    void testGetDepartmentAttendanceHistory_InvalidSort_Returns400() throws Exception {
        when(departmentAttendanceService.getDepartmentAttendanceHistory(eq(5L), any()))
                .thenThrow(new IllegalArgumentException("Invalid sort field 'organizationId'. Allowed fields: [date, checkInTime, checkOutTime, status]"));

        mockMvc.perform(get("/api/v1/attendance/departments/5/history")
                        .param("sortBy", "organizationId")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"));
    }
}
