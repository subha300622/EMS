package com.example.ems.attendance.controller;

import com.example.ems.attendance.entity.AttendanceLog;
import com.example.ems.attendance.service.AttendanceLogService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttendanceLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceLogService attendanceLogService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AttendanceLogController attendanceLogController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";

    private User currentUser;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceLogController).build();

        currentUser = new User();
        currentUser.setWorkEmail(EMAIL);

        employee = new Employee();
        employee.setId(1L);
        employee.setEmail(EMAIL);
        employee.setFullName("John Doe");

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(currentUser));
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(employee));
    }

    @Test
    public void testGetDailyLogsSelfSuccess() throws Exception {
        LocalDate today = LocalDate.now();
        AttendanceLog log1 = new AttendanceLog(employee, today, LocalTime.of(9, 0), "SWIPE_IN", "OFFICE_GATE");
        Page<AttendanceLog> pageResult = new PageImpl<>(List.of(log1), PageRequest.of(0, 50), 1);

        when(attendanceLogService.getDailyLogs(eq(1L), eq(today), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/attendance/employee/1/logs")
                        .header("Authorization", AUTH_HEADER)
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Swipe logs retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].type").value("SWIPE_IN"))
                .andExpect(jsonPath("$.data.content[0].location").value("OFFICE_GATE"));
    }

    @Test
    public void testGetDailyLogsOtherAsAdminSuccess() throws Exception {
        LocalDate today = LocalDate.now();
        Employee other = new Employee();
        other.setId(2L);
        other.setEmail("other@example.com");

        AttendanceLog log1 = new AttendanceLog(other, today, LocalTime.of(9, 0), "SWIPE_IN", "REMOTE_LOGIN");
        Page<AttendanceLog> pageResult = new PageImpl<>(List.of(log1), PageRequest.of(0, 50), 1);

        when(roleService.hasPermission(EMAIL, "attendance.read")).thenReturn(true);
        when(attendanceLogService.getDailyLogs(eq(2L), eq(today), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/attendance/employee/2/logs")
                        .header("Authorization", AUTH_HEADER)
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].type").value("SWIPE_IN"));
    }

    @Test
    public void testGetDailyLogsOtherForbidden() throws Exception {
        LocalDate today = LocalDate.now();

        when(roleService.hasPermission(EMAIL, "attendance.read")).thenReturn(false);
        when(roleService.hasPermission(EMAIL, "attendance.manage")).thenReturn(false);

        mockMvc.perform(get("/api/v1/attendance/employee/2/logs")
                        .header("Authorization", AUTH_HEADER)
                        .param("date", today.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
    }
}
