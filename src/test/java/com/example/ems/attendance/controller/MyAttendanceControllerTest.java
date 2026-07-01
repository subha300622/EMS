package com.example.ems.attendance.controller;

import com.example.ems.attendance.dto.CheckInRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.exception.DuplicateCheckInException;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyAttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MyAttendanceController myAttendanceController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";

    private User currentUser;
    private Employee employee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(myAttendanceController).build();

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
    public void testCheckInSuccess() throws Exception {
        Attendance attendance = new Attendance();
        attendance.setId(10L);
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.now());
        attendance.setStatus("PRESENT");
        attendance.setPunchInTime(LocalTime.of(9, 0));
        attendance.setNotes("First punch");

        when(attendanceService.checkIn(any(Employee.class), any(CheckInRequest.class))).thenReturn(attendance);

        mockMvc.perform(post("/api/v1/attendance/me/check-in")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"First punch\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Checked in successfully"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"))
                .andExpect(jsonPath("$.data.notes").value("First punch"));
    }

    @Test
    public void testCheckOutSuccess() throws Exception {
        Attendance attendance = new Attendance();
        attendance.setId(10L);
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.now());
        attendance.setStatus("PRESENT");
        attendance.setPunchInTime(LocalTime.of(9, 0));
        attendance.setPunchOutTime(LocalTime.of(17, 0));
        attendance.setNotes("Punch out");

        when(attendanceService.checkOut(any(Employee.class), eq("Punch out"))).thenReturn(attendance);

        mockMvc.perform(post("/api/v1/attendance/me/check-out")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Punch out\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Checked out successfully"))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    public void testGetMyAttendanceHistorySuccess() throws Exception {
        Attendance attendance = new Attendance();
        attendance.setId(10L);
        attendance.setEmployee(employee);
        attendance.setDate(LocalDate.now());
        attendance.setStatus("PRESENT");

        org.springframework.data.domain.Page<Attendance> mockPage = 
                new org.springframework.data.domain.PageImpl<>(List.of(attendance));

        when(attendanceService.getAttendanceByEmployeeIdPaginated(eq(1L), anyInt(), anyInt())).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/attendance/me")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    public void testCheckInDuplicateConflict() throws Exception {
        when(attendanceService.checkIn(any(Employee.class), any(CheckInRequest.class)))
                .thenThrow(new DuplicateCheckInException("Already checked in today"));

        mockMvc.perform(post("/api/v1/attendance/me/check-in")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Second punch\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Already checked in today"))
                .andExpect(jsonPath("$.errorCode").value("ATT_002"));
    }

    @Test
    public void testCheckInLocationRequired() throws Exception {
        when(attendanceService.checkIn(any(Employee.class), any(CheckInRequest.class)))
                .thenThrow(new IllegalArgumentException("Location required for GPS attendance"));

        mockMvc.perform(post("/api/v1/attendance/me/check-in")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\": \"Punch missing location\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Location required for GPS attendance"))
                .andExpect(jsonPath("$.errorCode").value("ATT_001"));
    }
}
