package com.example.ems.employee.controller;

import com.example.ems.asset.service.MyAssetService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.service.PerformanceService;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.service.TrainingAssignmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeamManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private MyScheduleService myScheduleService;

    @Mock
    private PerformanceService performanceService;

    @Mock
    private TrainingAssignmentService trainingService;

    @Mock
    private MyAssetService myAssetService;

    @InjectMocks
    private TeamManagementController teamManagementController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "sarah.chen@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(teamManagementController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetTeamDirectorySuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setFullName("Direct Report");
        report.setManager(manager);

        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/v1/team")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("Direct Report"));
    }

    @Test
    public void testGetTeamMemberDetailsSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setFullName("Direct Report");
        report.setManager(manager);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/v1/team/2")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Direct Report"));
    }

    @Test
    public void testGetTeamAttendanceSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setFullName("Direct Report");
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        Attendance attendance = new Attendance();
        attendance.setId(10L);
        attendance.setStatus("Present");
        when(attendanceService.getTodayAttendance(report)).thenReturn(Optional.of(attendance));

        mockMvc.perform(get("/api/v1/team/attendance")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }

    @Test
    public void testGetTeamSchedulesSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setEmail("report@example.com");
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/v1/team/schedules")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTeamPerformanceSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setEmail("report@example.com");
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        when(performanceService.getGoalsByEmployee(2L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/team/performance")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTeamTrainingsSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setEmail("report@example.com");
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        when(trainingService.getMyTrainings("report@example.com")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/team/trainings")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetTeamAssetsSuccess() throws Exception {
        mockPermission("team.read", true);

        Employee manager = new Employee();
        manager.setId(1L);
        manager.setEmail(EMAIL);
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(manager));

        Employee report = new Employee();
        report.setId(2L);
        report.setEmail("report@example.com");
        when(employeeRepository.findByManagerId(1L)).thenReturn(List.of(report));

        when(myAssetService.getAssignedAssets(eq(report), any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/team/assets")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
